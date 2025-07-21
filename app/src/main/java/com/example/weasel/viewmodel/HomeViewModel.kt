
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val popularThisWeek: List<Track>,
        val topSongsGlobal: List<Track>,
        val newReleases: List<Track>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val musicRepository: NewPipeMusicRepository,
    connectivityManager: AppConnectivityManager
) : ViewModel() {

    var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    val isOnline: StateFlow<Boolean> = connectivityManager.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            if (!isOnline.value) {
                uiState = HomeUiState.Error("You are offline. Please check your connection.")
                return@launch
            }

            uiState = HomeUiState.Loading

            try {
                val trendingDeferred = async { musicRepository.getTrendingTracks("PK", 20) }
                val topSongsDeferred = async { musicRepository.searchMusic("Top 50 Global playlist") }
                val newReleasesDeferred = async { musicRepository.searchMusic("New Music Friday playlist") }

                val trendingResult = trendingDeferred.await()
                val topSongsResult = topSongsDeferred.await()
                val newReleasesResult = newReleasesDeferred.await()

                uiState = HomeUiState.Success(
                    popularThisWeek = trendingResult.getOrNull()?.take(10) ?: emptyList(),
                    topSongsGlobal = topSongsResult.getOrNull()?.take(5) ?: emptyList(),
                    newReleases = newReleasesResult.getOrNull()?.take(4) ?: emptyList()
                )

            } catch (e: Exception) {
                uiState = HomeUiState.Error("Failed to load content: ${e.message}")
            }
        }
    }
}