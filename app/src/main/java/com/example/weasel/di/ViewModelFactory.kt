// In: app/src/main/java/com/example/weasel/di/ViewModelFactory.kt
package com.example.weasel.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.weasel.repository.LocalMusicRepository
import com.example.weasel.repository.MusicRepository
import com.example.weasel.repository.NewPipeMusicRepository
import com.example.weasel.repository.SettingsRepository
import com.example.weasel.util.AppConnectivityManager
import com.example.weasel.viewmodel.*

class ViewModelFactory(
    private val musicRepository: MusicRepository,
    private val localMusicRepository: LocalMusicRepository,
    private val connectivityManager: AppConnectivityManager,
    private val application: Application
) : ViewModelProvider.Factory {

    val settingsRepository by lazy { SettingsRepository(application) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(musicRepository as NewPipeMusicRepository, connectivityManager) as T
            }
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(musicRepository as NewPipeMusicRepository) as T
            }
            modelClass.isAssignableFrom(MusicPlayerViewModel::class.java) -> {
                MusicPlayerViewModel(musicRepository, localMusicRepository) as T
            }
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> {
                LibraryViewModel(localMusicRepository, application) as T
            }
            modelClass.isAssignableFrom(MessageViewModel::class.java) -> {
                MessageViewModel() as T
            }
            modelClass.isAssignableFrom(ThemeViewModel::class.java) -> {
                ThemeViewModel(settingsRepository) as T
            }
            modelClass.isAssignableFrom(PlaylistDetailViewModel::class.java) -> {
                val savedStateHandle = extras.createSavedStateHandle()
                PlaylistDetailViewModel(
                    savedStateHandle,
                    localMusicRepository,
                    musicRepository as NewPipeMusicRepository,
                    connectivityManager
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}