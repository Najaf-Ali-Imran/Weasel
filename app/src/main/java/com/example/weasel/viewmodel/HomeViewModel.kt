package com.example.weasel.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weasel.data.Track
import com.example.weasel.repository.NewPipeMusicRepository
import com.example.weasel.util.AppConnectivityManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val musicRepository: NewPipeMusicRepository,
    connectivityManager: AppConnectivityManager
) : ViewModel() {

    var popularThisWeek by mutableStateOf<List<Track>>(emptyList())
        private set
    var topSongsGlobal by mutableStateOf<List<Track>>(emptyList())
        private set
    var newReleases by mutableStateOf<List<Track>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val isOnline: StateFlow<Boolean> = connectivityManager.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            if (!isOnline.value) return@launch

            isLoading = true
            errorMessage = null
            try {
                val trendingDeferred = async { musicRepository.getTrendingTracks("PK", 20) }
                val topSongsDeferred = async { musicRepository.searchMusic("Top 50 Global playlist") }
                val newReleasesDeferred = async { musicRepository.searchMusic("New Music Friday playlist") }

                val trendingResult = trendingDeferred.await()
                val topSongsResult = topSongsDeferred.await()
                val newReleasesResult = newReleasesDeferred.await()

                trendingResult.onSuccess { tracks ->
                    popularThisWeek = tracks.take(10)
                }
                topSongsResult.onSuccess { tracks ->
                    topSongsGlobal = tracks.take(5)
                }
                newReleasesResult.onSuccess { tracks ->
                    newReleases = tracks.take(4)
                }

            } catch (e: Exception) {
                errorMessage = "Failed to load home content: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}