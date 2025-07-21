package com.example.weasel.ux

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.weasel.MainActivity
import com.example.weasel.viewmodel.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    libraryViewModel: LibraryViewModel,
    playerViewModel: MusicPlayerViewModel,
    contentPadding: PaddingValues,
    hasNewmessage: Boolean,
    onmessageClick: () -> Unit,
    onDownloadQueueClick: () -> Unit

) {
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var tracksToAdd by remember { mutableStateOf<List<String>>(emptyList()) }
    val playlists by libraryViewModel.userPlaylists.collectAsState()

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                playerViewModel.addTracksToPlaylist(playlistId, tracksToAdd)
                showAddToPlaylistDialog = false
                tracksToAdd = emptyList()
            }
        )
    }

    NavHost(navController = navController, startDestination = HOME_ROUTE) {
        val onSettingsClick: () -> Unit = { navController.navigate(SETTINGS_ROUTE) }

        composable(HOME_ROUTE) {
            HomeScreen(
                homeViewModel = homeViewModel,
                libraryViewModel = libraryViewModel,
                onTrackClick = { track -> playerViewModel.playTrack(navController.context, track, listOf(track)) },
                onArtistClick = { artistName ->
                    searchViewModel.search(artistName)
                    navController.navigate(SEARCH_ROUTE)
                },
                onSettingsClick = onSettingsClick,
                onDownloadQueueClick = onDownloadQueueClick,
                hasNewmessage = hasNewmessage,
                onmessageClick = onmessageClick,
                contentPadding = contentPadding
            )
        }

        composable(SEARCH_ROUTE) {
            val searchResults = (searchViewModel.uiState as? SearchUiState.Success)?.tracks ?: emptyList()
            SearchScreen(
                viewModel = searchViewModel,
                libraryViewModel = libraryViewModel,
                // --- FIX 2: Pass the playerViewModel to the SearchScreen ---
                playerViewModel = playerViewModel,
                onTrackClick = { track -> playerViewModel.playTrack(navController.context, track, searchResults) },
                onSettingsClick = onSettingsClick,
                contentPadding = contentPadding,
                onDownloadQueueClick = onDownloadQueueClick,
                hasNewmessage = hasNewmessage,
                onmessageClick = onmessageClick
            )
        }

        composable(LIBRARY_ROUTE) {
            LibraryScreen(
                viewModel = libraryViewModel,
                onCreatePlaylist = { name -> libraryViewModel.createNewPlaylist(name) },
                onPlaylistClick = { playlistId -> navController.navigate("$PLAYLIST_DETAIL_ROUTE/$playlistId") },
                onLocalSongsClick = { navController.navigate(LOCAL_SONGS_ROUTE) },
                onDownloadsClick = { navController.navigate(DOWNLOADS_ROUTE) },
                contentPadding = contentPadding
            )
        }

        composable(LOCAL_SONGS_ROUTE) {
            LocalSongsScreen(
                libraryViewModel = libraryViewModel,
                onTrackClick = { clickedTrack, playlist ->
                    playerViewModel.playTrack(navController.context, clickedTrack, playlist)
                },
                onNavigateUp = { navController.popBackStack() },
                onAddToPlaylist = { trackIds ->
                    tracksToAdd = trackIds
                    showAddToPlaylistDialog = true
                }
            )
        }

        composable(DOWNLOADS_ROUTE) {
            DownloadsScreen(
                libraryViewModel = libraryViewModel,
                onTrackClick = { clickedTrack, playlist ->
                    playerViewModel.playTrack(navController.context, clickedTrack, playlist)
                },
                onNavigateUp = { navController.popBackStack() }
            )
        }

        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                libraryViewModel = libraryViewModel,
                onNavigateUp = { navController.popBackStack() },
                onTrackClick = { track -> playerViewModel.playTrack(navController.context, track, listOf(track)) },
                contentPadding = contentPadding
            )
        }

        composable(
            route = "$PLAYLIST_DETAIL_ROUTE/{playlistId}",
            arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
        ) {
            val factory = (navController.context as MainActivity).viewModelFactory
            val detailViewModel: PlaylistDetailViewModel = viewModel(factory = factory)
            PlaylistDetailScreen(
                viewModel = detailViewModel,
                onTrackClick = { track, tracks -> playerViewModel.playTrack(navController.context, track, tracks) },
                onNavigateUp = { navController.popBackStack() },
            )
        }

        composable("$ADD_SONGS_ROUTE/{playlistId}") {
            val factory = (navController.context as MainActivity).viewModelFactory
            val detailViewModel: PlaylistDetailViewModel = viewModel(factory = factory)
            AddSongsScreen(
                libraryViewModel = libraryViewModel,
                onNavigateUp = { navController.popBackStack() },
                onAddSongs = { trackIds ->
                }
            )
        }
    }
}