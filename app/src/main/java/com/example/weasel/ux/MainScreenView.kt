package com.example.weasel.ux

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.weasel.viewmodel.HomeViewModel
import com.example.weasel.viewmodel.LibraryViewModel
import com.example.weasel.viewmodel.MusicPlayerViewModel
import com.example.weasel.viewmodel.SearchViewModel
import kotlinx.coroutines.launch
import com.example.weasel.viewmodel.MessageViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weasel.MainActivity
import androidx.compose.ui.platform.LocalContext

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
    val messageViewModel: MessageViewModel = viewModel(factory = (LocalContext.current as MainActivity).viewModelFactory)

    val navController = rememberNavController()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    val message by messageViewModel.message.collectAsState()
    val hasMessage = message?.text?.isNotBlank() == true

    if (showMessageDialog && message != null) {
        MessageDialog(message = message!!, onDismiss = { showMessageDialog = false })
    }
    val currentTrack = playerViewModel.currentTrack

    LaunchedEffect(Unit) {
        playerViewModel.initializePlayer(navController.context)
    }
    if (showDownloadQueueDialog) {
        val downloadQueue by libraryViewModel.downloadQueue.collectAsState()
        DownloadQueueDialog(
            downloads = downloadQueue,
            onDismiss = { showDownloadQueueDialog = false }
        )
    }
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent
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
                            selectedIconColor = MaterialTheme.colorScheme.background,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.tertiary,
                            unselectedTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
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
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            NowPlayingScreen(
                viewModel = playerViewModel,
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