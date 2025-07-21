package com.example.weasel.ux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weasel.MainActivity
import com.example.weasel.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenView(
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    libraryViewModel: LibraryViewModel,
    playerViewModel: MusicPlayerViewModel,
    hasNewmessage: Boolean,
    onmessageClick: () -> Unit
) {
    var showDownloadQueueDialog by remember { mutableStateOf(false) }
    val downloadQueue by libraryViewModel.downloadQueue.collectAsState()

    val themeViewModel: ThemeViewModel = viewModel(factory = (LocalContext.current as MainActivity).viewModelFactory)
    val isNavBarTranslucent by themeViewModel.isNavBarTranslucent.collectAsState()

    val navController = rememberNavController()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    val currentTrack = playerViewModel.currentTrack

    LaunchedEffect(Unit) {
        playerViewModel.initializePlayer(navController.context)
    }
    if (showDownloadQueueDialog) {
        DownloadQueueDialog(
            downloads = downloadQueue,
            onDismiss = { showDownloadQueueDialog = false }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isNavBarTranslucent) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    )
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = if (isNavBarTranslucent) NavigationBarDefaults.Elevation else 0.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.iconResId),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            homeViewModel = homeViewModel,
            searchViewModel = searchViewModel,
            libraryViewModel = libraryViewModel,
            playerViewModel = playerViewModel,
            contentPadding = innerPadding,
            onDownloadQueueClick = { showDownloadQueueDialog = true },
            hasNewmessage = hasNewmessage,
            onmessageClick = onmessageClick
        )

        if (currentTrack != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                MiniPlayer(
                    viewModel = playerViewModel,
                    onExpandClick = { showBottomSheet = true }
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null
        ) {
            NowPlayingScreen(
                viewModel = playerViewModel,
                libraryViewModel = libraryViewModel,
                onBackClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }
}