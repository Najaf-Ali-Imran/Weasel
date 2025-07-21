package com.example.weasel.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.weasel.data.DownloadWorker
import com.example.weasel.data.Playlist
import com.example.weasel.data.Track
import com.example.weasel.data.local.HistoryEntry
import com.example.weasel.repository.LocalMusicRepository
import com.example.weasel.repository.ThemeSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

const val LIKED_SONGS_PLAYLIST_NAME = "Liked Songs"

data class DownloadProgress(
    val title: String,
    val artist: String,
    val progress: Int
)
class LibraryViewModel(
    private val localMusicRepository: LocalMusicRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    val history: StateFlow<List<HistoryEntry>> = localMusicRepository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _likedSongsPlaylist = MutableStateFlow<Playlist?>(null)
    val likedSongsPlaylist: StateFlow<Playlist?> = _likedSongsPlaylist

    val userPlaylists: StateFlow<List<Playlist>> = localMusicRepository.getPlaylists()
        .map { list -> list.filter { it.name != LIKED_SONGS_PLAYLIST_NAME } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _localSongs = MutableStateFlow<List<Track>>(emptyList())
    val localSongs: StateFlow<List<Track>> = _localSongs

    val downloadedTracks: StateFlow<List<Track>> = localMusicRepository.getDownloadedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _downloadQueue = MutableStateFlow<List<DownloadProgress>>(emptyList())
    val downloadQueue: StateFlow<List<DownloadProgress>> = _downloadQueue

    init {
        observeDownloadQueue()
        createLikedSongsPlaylistIfNeeded()
    }

    private fun createLikedSongsPlaylistIfNeeded() {
        viewModelScope.launch {
            var likedPlaylist = localMusicRepository.getPlaylistByName(LIKED_SONGS_PLAYLIST_NAME)
            if (likedPlaylist == null) {
                localMusicRepository.createPlaylist(LIKED_SONGS_PLAYLIST_NAME)
                likedPlaylist = localMusicRepository.getPlaylistByName(LIKED_SONGS_PLAYLIST_NAME)
            }
            _likedSongsPlaylist.value = likedPlaylist
        }
    }

    fun addTrackToLikedSongs(track: Track) {
        viewModelScope.launch {
            _likedSongsPlaylist.value?.let { playlist ->
                localMusicRepository.insertAndAddTracksToPlaylist(playlist.id, listOf(track))
                Toast.makeText(application, "Added to Liked Songs", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            localMusicRepository.insertAndAddTracksToPlaylist(playlistId, listOf(track))
            Toast.makeText(application, "Added to playlist", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadTrack(track: Track) {
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
            workManager.enqueue(downloadRequest)
            Toast.makeText(application, "Download started.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(application, "Already downloaded.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeDownloadQueue() {
        workManager.getWorkInfosByTagLiveData(DownloadWorker.DOWNLOAD_TAG)
            .observeForever { workInfos ->
                val runningDownloads = workInfos
                    .filter { !it.state.isFinished }
                    .map { workInfo ->
                        val progressData = workInfo.progress
                        val progress = progressData.getInt(DownloadWorker.KEY_PROGRESS, 0)
                        val title = progressData.getString(DownloadWorker.KEY_TRACK_TITLE) ?: "Preparing..."
                        val artist = progressData.getString(DownloadWorker.KEY_TRACK_ARTIST) ?: ""
                        DownloadProgress(title, artist, progress)
                    }
                _downloadQueue.value = runningDownloads
            }
    }

    fun loadLocalSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            _localSongs.value = localMusicRepository.getLocalAudioFiles()
        }
    }

    fun createNewPlaylist(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                localMusicRepository.createPlaylist(name)
            }
        }
    }
}