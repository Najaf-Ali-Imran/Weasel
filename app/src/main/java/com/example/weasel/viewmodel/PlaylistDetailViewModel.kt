package com.example.weasel.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.workDataOf
import com.example.weasel.data.DownloadWorker
import com.example.weasel.data.Track
import com.example.weasel.data.PlaylistWithTracks
import com.example.weasel.repository.LocalMusicRepository
import com.example.weasel.repository.NewPipeMusicRepository
import com.example.weasel.util.AppConnectivityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val localMusicRepository: LocalMusicRepository,
    private val newPipeMusicRepository: NewPipeMusicRepository,
    connectivityManager: AppConnectivityManager
) : ViewModel() {

    private val playlistId: Long = savedStateHandle.get<Long>("playlistId") ?: -1L

    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder

    val isOnline: StateFlow<Boolean> = connectivityManager.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)


    val playlistTracks: StateFlow<List<Track>> =
        localMusicRepository.getPlaylistWithTracks(playlistId)
            .filterNotNull()
            .combine(_sortOrder) { playlistWithTracks: PlaylistWithTracks, order: SortOrder ->
                when (order) {
                    SortOrder.DEFAULT -> playlistWithTracks.tracks
                    SortOrder.A_TO_Z -> playlistWithTracks.tracks.sortedBy { it.title }
                    SortOrder.ARTIST -> playlistWithTracks.tracks.sortedBy { it.artist }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistName: StateFlow<String> =
        localMusicRepository.getPlaylistWithTracks(playlistId)
            .filterNotNull()
            .map { it.playlist.name }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _recommendations = MutableStateFlow<List<Track>>(emptyList())
    val recommendations: StateFlow<List<Track>> = _recommendations

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations

    init {
        viewModelScope.launch {
            playlistTracks.collect { tracks ->
                if (isOnline.value && tracks.isNotEmpty() && recommendations.value.isEmpty()) {
                    fetchRecommendations()
                }
            }
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            localMusicRepository.deletePlaylist(playlistId)
        }
    }

    fun downloadAllTracks(context: Context) {
        viewModelScope.launch {
            val tracksToDownload = playlistTracks.value
            tracksToDownload.forEach { track ->
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
    }

    fun removeSongsFromPlaylist(trackIds: List<String>) {
        viewModelScope.launch {
            localMusicRepository.removeTracksFromPlaylist(playlistId, trackIds)
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun addSongsToPlaylist(trackIds: List<String>) {
        viewModelScope.launch {
            localMusicRepository.addTracksToPlaylist(playlistId, trackIds)
        }
    }

    fun fetchRecommendations() {
        if (_isLoadingRecommendations.value || !isOnline.value) return
        viewModelScope.launch {
            _isLoadingRecommendations.value = true
            val firstTrackId = playlistTracks.value.firstOrNull()?.id
            if (firstTrackId != null && !firstTrackId.startsWith("content://")) {
                val result = newPipeMusicRepository.getRelatedStreams(firstTrackId)
                result.onSuccess { _recommendations.value = it.take(10) }
            }
            _isLoadingRecommendations.value = false
        }
    }
}

enum class SortOrder {
    DEFAULT, A_TO_Z, ARTIST
}