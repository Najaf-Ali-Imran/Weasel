package com.example.weasel.viewmodel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.weasel.data.DownloadWorker
import com.example.weasel.data.Track
import com.example.weasel.player.MusicPlayerService
import com.example.weasel.repository.LocalMusicRepository
import com.example.weasel.repository.MusicRepository
import com.example.weasel.repository.NewPipeMusicRepository
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MusicPlayerViewModel(
    private val musicRepository: MusicRepository,
    private val localMusicRepository: LocalMusicRepository
) : ViewModel() {

    var currentTrack by mutableStateOf<Track?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var currentPosition by mutableLongStateOf(0L)
        private set
    var duration by mutableLongStateOf(0L)
        private set
    var isBuffering by mutableStateOf(false)
        private set
    var isShuffleEnabled by mutableStateOf(false)
        private set
    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set

    private var playlist = mutableListOf<Track>()
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var isTrackingPosition = false

    private var backgroundLoadingJob: Job? = null
    private var relatedTracksCache = mutableListOf<Track>()
    private var audioUrlCache = mutableMapOf<String, String>()
    private var currentContextType = ContextType.SINGLE_SONG
    private var originalTrackList = listOf<Track>()

    private enum class ContextType {
        SINGLE_SONG,
        PLAYLIST
    }

    fun initializePlayer(context: Context) {
        if (mediaControllerFuture == null) {
            val sessionToken = SessionToken(context, android.content.ComponentName(context, MusicPlayerService::class.java))
            mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            mediaControllerFuture?.addListener({
                mediaController = mediaControllerFuture?.get()
                setupPlayerListener()
            }, MoreExecutors.directExecutor())
        }
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
                if (playing) {
                    isBuffering = false
                    startPositionTracking()
                } else {
                    isTrackingPosition = false
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    duration = mediaController?.duration ?: 0L
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let { item ->
                    val trackIndex = playlist.indexOfFirst { it.id == item.mediaId }
                    if (trackIndex != -1) {
                        currentTrack = playlist[trackIndex]
                        duration = mediaController?.duration ?: 0L

                        if (currentContextType == ContextType.SINGLE_SONG && trackIndex >= playlist.size - 2) {
                            loadMoreTracksInBackground()
                        }
                    }
                }
            }
        })
    }

    fun playTrack(context: Context, track: Track, trackList: List<Track>) {
        viewModelScope.launch {
            backgroundLoadingJob?.cancel()

            currentContextType = if (trackList.size == 1) ContextType.SINGLE_SONG else ContextType.PLAYLIST
            originalTrackList = trackList

            playTrackInstantly(context, track, trackList)
        }
    }

    private suspend fun playTrackInstantly(context: Context, track: Track, trackList: List<Track>) {
        if (isBuffering && track.id == currentTrack?.id) return

        isBuffering = true
        currentTrack = track
        playlist = trackList.toMutableList()
        localMusicRepository.addToHistory(track)

        initializePlayer(context)

        val trackAudioUrl = getAudioUrlForTrack(track)

        if (trackAudioUrl.isEmpty()) {
            isBuffering = false
            return
        }

        audioUrlCache[track.id] = trackAudioUrl

        val firstMediaItem = createMediaItem(track, trackAudioUrl)
        mediaController?.clearMediaItems()
        mediaController?.setMediaItem(firstMediaItem)
        mediaController?.prepare()
        mediaController?.play()

        startBackgroundPlaylistLoading(track, trackList)
    }

    private fun startBackgroundPlaylistLoading(currentTrack: Track, trackList: List<Track>) {
        backgroundLoadingJob = viewModelScope.launch {
            try {
                delay(1000)

                if (currentContextType == ContextType.PLAYLIST) {
                    loadRemainingPlaylistTracks(currentTrack, trackList)
                } else {
                    loadRelatedTracksForSingleSong(currentTrack)
                }
            } catch (e: Exception) {
            }
        }
    }

    private suspend fun loadRemainingPlaylistTracks(currentTrack: Track, trackList: List<Track>) {
        val subsequentTracks = trackList.filter { it.id != currentTrack.id }

        subsequentTracks.forEach { track ->
            // Use the corrected helper function here too
            val audioUrl = getAudioUrlForTrack(track)
            if (audioUrl.isNotEmpty() && !audioUrlCache.containsKey(track.id)) {
                audioUrlCache[track.id] = audioUrl
                val mediaItem = createMediaItem(track, audioUrl)
                mediaController?.addMediaItem(mediaItem)
            }
        }
    }

    private suspend fun loadRelatedTracksForSingleSong(track: Track) {
        if (musicRepository is NewPipeMusicRepository) {
            musicRepository.getRelatedStreams(track.id).onSuccess { relatedTracks ->
                relatedTracksCache.addAll(relatedTracks.take(10))

                // Preload URLs for first 3 related tracks
                relatedTracks.take(3).forEach { relatedTrack ->
                    if (!audioUrlCache.containsKey(relatedTrack.id)) {
                        musicRepository.getAudioStreamUrl(relatedTrack.id).getOrNull()?.let { url ->
                            audioUrlCache[relatedTrack.id] = url
                            playlist.add(relatedTrack)
                            mediaController?.addMediaItem(createMediaItem(relatedTrack, url))
                        }
                    }
                }
            }
        }
    }

    private fun loadMoreTracksInBackground() {
        if (currentContextType == ContextType.SINGLE_SONG && relatedTracksCache.isEmpty()) return

        viewModelScope.launch {
            try {
                val tracksToLoad = when (currentContextType) {
                    ContextType.SINGLE_SONG -> relatedTracksCache.take(3)
                    ContextType.PLAYLIST -> {
                        if (musicRepository is NewPipeMusicRepository && currentTrack != null) {
                            musicRepository.getRelatedStreams(currentTrack!!.id).getOrNull()?.take(3) ?: emptyList()
                        } else {
                            emptyList()
                        }
                    }
                }

                tracksToLoad.forEach { track ->
                    if (!audioUrlCache.containsKey(track.id)) {
                        val audioUrl = musicRepository.getAudioStreamUrl(track.id).getOrNull() ?: ""
                        if (audioUrl.isNotEmpty()) {
                            audioUrlCache[track.id] = audioUrl
                            playlist.add(track)
                            mediaController?.addMediaItem(createMediaItem(track, audioUrl))
                        }
                    }
                }

                if (currentContextType == ContextType.SINGLE_SONG) {
                    repeat(tracksToLoad.size) {
                        if (relatedTracksCache.isNotEmpty()) {
                            relatedTracksCache.removeAt(0)
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun downloadCurrentTrack(context: Context) {
        currentTrack?.let { track ->
            if (!track.isDownloaded) {
                val data = workDataOf(
                    DownloadWorker.KEY_TRACK_ID to track.id,
                    DownloadWorker.KEY_TRACK_TITLE to track.title,
                    DownloadWorker.KEY_TRACK_ARTIST to track.artist
                )
                val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(data)
                    .addTag(DownloadWorker.DOWNLOAD_TAG)
                    .build()
                WorkManager.getInstance(context).enqueue(downloadRequest)
            }
        }
    }

    fun addTracksToPlaylist(playlistId: Long, trackIds: List<String>) {
        viewModelScope.launch {
            localMusicRepository.addTracksToPlaylist(playlistId, trackIds)
        }
    }

    fun addCurrentTrackToPlaylist(playlistId: Long) {
        viewModelScope.launch {
            currentTrack?.let { track ->
                localMusicRepository.insertTrack(track)
                localMusicRepository.addTracksToPlaylist(playlistId, listOf(track.id))
            }
        }
    }

    fun stopAndClearPlayer() {
        mediaController?.stop()
        mediaController?.clearMediaItems()
        currentTrack = null
        isPlaying = false
        relatedTracksCache.clear()
        audioUrlCache.clear()
        backgroundLoadingJob?.cancel()
    }

    fun play() { mediaController?.play() }
    fun pause() { mediaController?.pause() }

    fun skipToNext() {
        val currentIndex = mediaController?.currentMediaItemIndex ?: 0
        val hasNextTrack = currentIndex < playlist.size - 1

        if (hasNextTrack) {
            mediaController?.seekToNextMediaItem()
        } else {
            loadMoreTracksInBackground()
            viewModelScope.launch {
                delay(500)
                if (currentIndex < playlist.size - 1) {
                    mediaController?.seekToNextMediaItem()
                }
            }
        }
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        currentPosition = positionMs
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        mediaController?.shuffleModeEnabled = isShuffleEnabled
    }

    fun toggleRepeat() {
        repeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = repeatMode
    }

    private fun startPositionTracking() {
        if (isTrackingPosition) return
        isTrackingPosition = true
        viewModelScope.launch {
            while (isPlaying) {
                currentPosition = mediaController?.currentPosition ?: 0L
                if (duration <= 0) {
                    duration = mediaController?.duration ?: 0L
                }
                delay(1000)
            }
            isTrackingPosition = false
        }
    }

    private fun addTrackToMediaController(track: Track, atIndex: Int = -1) {
        viewModelScope.launch {
            val audioUrl = getAudioUrlForTrack(track)
            if (audioUrl.isNotEmpty()) {
                val mediaItem = createMediaItem(track, audioUrl)
                if (atIndex == -1 || atIndex > playlist.size) {
                    // Add to end
                    mediaController?.addMediaItem(mediaItem)
                    playlist.add(track)
                } else {
                    // Add at a specific index
                    mediaController?.addMediaItem(atIndex, mediaItem)
                    playlist.add(atIndex, track)
                }
            }
        }
    }

    fun addTrackToQueue(track: Track) {
        addTrackToMediaController(track)
    }

    fun playTrackNext(track: Track) {
        val nextIndex = (mediaController?.currentMediaItemIndex ?: -1) + 1
        addTrackToMediaController(track, nextIndex)
    }

    private suspend fun getAudioUrlForTrack(track: Track): String {
        return when {
            track.isDownloaded && !track.localPath.isNullOrBlank() -> track.localPath

            track.id.startsWith("content://") -> track.id

            audioUrlCache.containsKey(track.id) -> audioUrlCache[track.id]!!

            else -> {
                val url = musicRepository.getAudioStreamUrl(track.id).getOrNull() ?: ""
                if (url.isNotEmpty()) {
                    audioUrlCache[track.id] = url
                }
                url
            }
        }
    }

    private fun createMediaItem(track: Track, audioUrl: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setArtworkUri(android.net.Uri.parse(track.thumbnailUrl))
            .build()
        return MediaItem.Builder()
            .setUri(audioUrl)
            .setMediaId(track.id)
            .setMediaMetadata(metadata)
            .build()
    }

    override fun onCleared() {
        super.onCleared()
        isTrackingPosition = false
        backgroundLoadingJob?.cancel()
        relatedTracksCache.clear()
        audioUrlCache.clear()
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }
}