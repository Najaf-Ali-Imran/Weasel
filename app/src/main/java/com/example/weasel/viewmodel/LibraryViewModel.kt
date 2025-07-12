package com.example.weasel.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.weasel.data.DownloadWorker
import com.example.weasel.data.Playlist
import com.example.weasel.data.Track
import com.example.weasel.data.local.HistoryEntry
import com.example.weasel.repository.LocalMusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DownloadProgress(
    val title: String,
    val artist: String,
    val progress: Int
)

class LibraryViewModel(
    private val localMusicRepository: LocalMusicRepository,
    application: Application
) : AndroidViewModel(application) {

    private val workManager = WorkManager.getInstance(application)

    val history: StateFlow<List<HistoryEntry>> = localMusicRepository.getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = localMusicRepository.getPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _localSongs = MutableStateFlow<List<Track>>(emptyList())
    val localSongs: StateFlow<List<Track>> = _localSongs

    val downloadedTracks: StateFlow<List<Track>> = localMusicRepository.getDownloadedTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _downloadQueue = MutableStateFlow<List<DownloadProgress>>(emptyList())
    val downloadQueue: StateFlow<List<DownloadProgress>> = _downloadQueue

    init {
        observeDownloadQueue()
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