package com.example.weasel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import coil.compose.LocalImageLoader
import coil.imageLoader
// --- FIX: REMOVE UNUSED IMPORT ---
// import coil.imageLoader
import com.example.weasel.data.local.AppDatabase
import com.example.weasel.di.ViewModelFactory
import com.example.weasel.repository.LocalMusicRepository
import com.example.weasel.repository.NewPipeMusicRepository
import com.example.weasel.repository.ThemeSetting
import com.example.weasel.ui.theme.WeaselTheme
import com.example.weasel.util.AppConnectivityManager
import com.example.weasel.ux.BetaDisclaimerDialog
import com.example.weasel.ux.MainScreenView
import com.example.weasel.ux.MessageDialog
import com.example.weasel.viewmodel.*

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val localRepository by lazy { LocalMusicRepository(database.musicDao(), this) }
    private val musicRepository by lazy { NewPipeMusicRepository() }
    private val connectivityManager by lazy { AppConnectivityManager(this) }

    internal val viewModelFactory by lazy {
        ViewModelFactory(musicRepository, localRepository, connectivityManager, application)
    }

    private val themeViewModel: ThemeViewModel by viewModels { viewModelFactory }
    private val messageViewModel: MessageViewModel by viewModels { viewModelFactory }
    private val homeViewModel: HomeViewModel by viewModels { viewModelFactory }
    private val searchViewModel: SearchViewModel by viewModels { viewModelFactory }
    private val libraryViewModel: LibraryViewModel by viewModels { viewModelFactory }
    private val playerViewModel: MusicPlayerViewModel by viewModels { viewModelFactory }


    private val requestLegacyPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                libraryViewModel.loadLocalSongs()
            } else {
                // TODO: Handle permission denial
            }
        }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isAudioGranted = permissions[Manifest.permission.READ_MEDIA_AUDIO] ?: false

            if (isAudioGranted) {
                libraryViewModel.loadLocalSongs()
            } else {
                // TODO: Show a message explaining why the feature is unavailable.
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val isNavBarTranslucent = viewModelFactory.settingsRepository.isNavBarTranslucent()
        WindowCompat.setDecorFitsSystemWindows(window, !isNavBarTranslucent)

        askForPermissions()

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)

        setContent {
            val imageLoader = (application as WeaselApplication).imageLoader

            val themeSetting by themeViewModel.theme.collectAsState()
            val useDarkTheme = when (themeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            WeaselTheme(darkTheme = useDarkTheme) {
                CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        var showDisclaimer by remember { mutableStateOf(isFirstLaunch) }
                        var showMessageDialog by remember { mutableStateOf(false) }

                        val message by messageViewModel.message.collectAsState()
                        val hasMessage = message?.text?.isNotBlank() == true

                        MainScreenView(
                            homeViewModel = homeViewModel,
                            searchViewModel = searchViewModel,
                            libraryViewModel = libraryViewModel,
                            playerViewModel = playerViewModel,
                            hasNewmessage = hasMessage,
                            onmessageClick = { showMessageDialog = true }
                        )

                        if (showDisclaimer) {
                            BetaDisclaimerDialog(onDismiss = {
                                showDisclaimer = false
                                prefs.edit().putBoolean("is_first_launch", false).apply()
                            })
                        }

                        if (showMessageDialog && message != null) {
                            MessageDialog(
                                message = message!!,
                                onDismiss = { showMessageDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun askForPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                val permissionsToRequest = arrayOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestMultiplePermissionsLauncher.launch(permissionsToRequest)
                } else {
                    libraryViewModel.loadLocalSongs()
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestLegacyPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                } else {
                    libraryViewModel.loadLocalSongs()
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestLegacyPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    libraryViewModel.loadLocalSongs()
                }
            }
            else -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestLegacyPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    libraryViewModel.loadLocalSongs()
                }
            }
        }
    }
}