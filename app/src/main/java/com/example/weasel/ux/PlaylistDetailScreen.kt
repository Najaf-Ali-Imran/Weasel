package com.example.weasel.ux

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.weasel.R
import com.example.weasel.data.Track
import com.example.weasel.viewmodel.PlaylistDetailViewModel
import com.example.weasel.viewmodel.SortOrder
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel,
    onTrackClick: (Track, List<Track>) -> Unit,
    onNavigateUp: () -> Unit
) {
    val playlistTracks by viewModel.playlistTracks.collectAsState()
    val playlistName by viewModel.playlistName.collectAsState()
    val playlistThumbnailUrl by viewModel.playlistThumbnailUrl.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()
    val isLoadingRecommendations by viewModel.isLoadingRecommendations.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()
    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsState()

    val playableTracks = remember(playlistTracks, isOnline) {
        playlistTracks.filter { it.isDownloaded || isOnline }
    }

    val selectedTrackIds = remember { mutableStateListOf<String>() }
    val isInSelectionMode = selectedTrackIds.isNotEmpty()

    var showOptionsSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showAddSongsOptionsSheet by remember { mutableStateOf(false) }
    var showAddFromLibraryOverlay by remember { mutableStateOf(false) }
    var showAddFromSearchOverlay by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
            viewModel.updatePlaylistDetails(playlistName, it.toString())
        }
    }

    if (showEditTitleDialog) {
        EditPlaylistDialog(
            playlistName = playlistName,
            onDismiss = { showEditTitleDialog = false },
            onSave = { newName -> viewModel.updatePlaylistDetails(newName, null) }
        )
    }

    if (showOptionsSheet) {
        PlaylistOptionsBottomSheet(
            onDismiss = { showOptionsSheet = false },
            onEditTitle = { showOptionsSheet = false; showEditTitleDialog = true },
            onEditThumbnail = { showOptionsSheet = false; imagePickerLauncher.launch("image/*") },
            onDelete = { showOptionsSheet = false; viewModel.deletePlaylist(); onNavigateUp() }
        )
    }

    if (showSortSheet) {
        SortOptionsBottomSheet(
            currentSortOrder = currentSortOrder,
            onDismiss = { showSortSheet = false },
            onSortSelected = { viewModel.setSortOrder(it) }
        )
    }

    if (showAddSongsOptionsSheet) {
        AddSongsOptionsBottomSheet(
            onDismiss = { showAddSongsOptionsSheet = false },
            onAddFromLibrary = { showAddSongsOptionsSheet = false; showAddFromLibraryOverlay = true },
            onAddFromSearch = { showAddSongsOptionsSheet = false; showAddFromSearchOverlay = true }
        )
    }

    if (showAddFromLibraryOverlay) {
        Dialog(
            onDismissRequest = { showAddFromLibraryOverlay = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AddSongsDialogContent(
                viewModel = viewModel,
                onDismiss = { showAddFromLibraryOverlay = false },
                onAddSongs = { tracks -> viewModel.addTracksToPlaylist(tracks) }
            )
        }
    }

    if (showAddFromSearchOverlay) {
        Dialog(
            onDismissRequest = { showAddFromSearchOverlay = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            AddFromSearchDialogContent(
                viewModel = viewModel,
                onDismiss = { showAddFromSearchOverlay = false },
                onAddSongs = { tracks -> viewModel.addTracksToPlaylist(tracks) }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    val icon = if (isInSelectionMode) Icons.Default.Cancel else Icons.AutoMirrored.Filled.ArrowBack
                    IconButton(onClick = {
                        if (isInSelectionMode) selectedTrackIds.clear() else onNavigateUp()
                    }) {
                        Icon(icon, contentDescription = if (isInSelectionMode) "Cancel" else "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 150.dp
            )
        ) {
            item {
                PlaylistHeader(
                    imageUrl = playlistThumbnailUrl ?: playlistTracks.firstOrNull()?.thumbnailUrl,
                    playlistName = playlistName
                )
            }
            item {
                ActionButtonsSection(
                    onShuffleClick = {
                        val shuffled = playableTracks.shuffled()
                        shuffled.firstOrNull()?.let { onTrackClick(it, shuffled) }
                    },
                    onPlayClick = {
                        playableTracks.firstOrNull()?.let { onTrackClick(it, playableTracks) }
                    },
                    onSortClick = { showSortSheet = true },
                    onAddSongsClick = { showAddSongsOptionsSheet = true },
                    onDownloadClick = {
                        viewModel.downloadAllTracks(context)
                        Toast.makeText(context, "Download started.", Toast.LENGTH_SHORT).show()
                    },
                    onMoreClick = { showOptionsSheet = true }
                )
            }

            if (isInSelectionMode) {
                item {
                    Button(
                        onClick = {
                            viewModel.removeSongsFromPlaylist(selectedTrackIds.toList())
                            selectedTrackIds.clear()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Remove ${selectedTrackIds.size} Songs")
                    }
                }
            }

            if (playlistTracks.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp), contentAlignment = Alignment.Center
                    ) {
                        Text("This playlist is empty. Add some songs!", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(items = playlistTracks, key = { it.id }) { track ->
                    val isSelected = track.id in selectedTrackIds
                    val isPlayable = track.isDownloaded || isOnline

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isPlayable) {
                                        if (isInSelectionMode) {
                                            if (isSelected) selectedTrackIds.remove(track.id) else selectedTrackIds.add(track.id)
                                        } else {
                                            onTrackClick(track, playableTracks)
                                        }
                                    } else {
                                        Toast.makeText(context, "This song is not available offline.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onLongClick = {
                                    if (isPlayable && !isSelected) {
                                        selectedTrackIds.add(track.id)
                                    }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isInSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) selectedTrackIds.add(track.id) else selectedTrackIds.remove(track.id)
                                },
                                enabled = isPlayable,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        TrackListItem(track = track, onTrackClicked = {}, isClickable = false, enabled = isPlayable)
                    }
                }
            }

            if (isOnline) {
                if (recommendations.isNotEmpty() || isLoadingRecommendations) {
                    item { RecommendedSongsSection() }
                }
                if (isLoadingRecommendations) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp), contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                } else {
                    items(items = recommendations, key = { "rec_${it.id}" }) { track ->
                        TrackListItem(
                            track = track,
                            onTrackClicked = { onTrackClick(track, recommendations) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistHeader(imageUrl: String?, playlistName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Playlist artwork",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_library_music)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(playlistName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionButtonsSection(
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSortClick: () -> Unit,
    onAddSongsClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onAddSongsClick) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Songs")
            }
            IconButton(onClick = onDownloadClick) {
                Icon(Icons.Default.Download, contentDescription = "Download Playlist")
            }
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onShuffleClick) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(28.dp)
                    )
                }
                FloatingActionButton(
                    onClick = onPlayClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onSortClick, shape = RoundedCornerShape(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapVert, contentDescription = "Sort", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sort")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistOptionsBottomSheet(
    onDismiss: () -> Unit,
    onEditTitle: () -> Unit,
    onEditThumbnail: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text("Options", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            BottomSheetListItem(icon = Icons.Default.Edit, text = "Edit title", onClick = onEditTitle)
            BottomSheetListItem(icon = Icons.Default.Image, text = "Edit thumbnail", onClick = onEditThumbnail)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            BottomSheetListItem(icon = Icons.Default.Delete, text = "Delete playlist", onClick = onDelete)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOptionsBottomSheet(
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onSortSelected: (SortOrder) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text("Sort by", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            SortBottomSheetItem("Default", currentSortOrder == SortOrder.DEFAULT) {
                onSortSelected(SortOrder.DEFAULT); onDismiss()
            }
            SortBottomSheetItem("Title (A-Z)", currentSortOrder == SortOrder.A_TO_Z) {
                onSortSelected(SortOrder.A_TO_Z); onDismiss()
            }
            SortBottomSheetItem("Artist", currentSortOrder == SortOrder.ARTIST) {
                onSortSelected(SortOrder.ARTIST); onDismiss()
            }
        }
    }
}

@Composable
private fun SortBottomSheetItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(text, color = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current)
        },
        modifier = Modifier.clickable(onClick = onClick),
        trailingContent = {
            if (isSelected) {
                Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}



@Composable
private fun RecommendedSongsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Recommended Songs", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Based on songs in this playlist", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
fun EditPlaylistDialog(playlistName: String, onDismiss: () -> Unit, onSave: (newName: String) -> Unit) {
    var name by remember { mutableStateOf(playlistName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Playlist Title") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(name); onDismiss() }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSongsOptionsBottomSheet(
    onDismiss: () -> Unit,
    onAddFromLibrary: () -> Unit,
    onAddFromSearch: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text("Add songs", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            BottomSheetListItem(icon = Icons.Default.LibraryMusic, text = "From Library", onClick = onAddFromLibrary)
            BottomSheetListItem(icon = Icons.Default.Search, text = "From Search", onClick = onAddFromSearch)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsDialogContent(
    viewModel: PlaylistDetailViewModel,
    onDismiss: () -> Unit,
    onAddSongs: (List<Track>) -> Unit
) {
    val localSongs by viewModel.localSongs.collectAsState()
    val selectedTracks = remember { mutableStateListOf<Track>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add from Library") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTracks.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onAddSongs(selectedTracks.toList())
                        onDismiss()
                    },
                    text = { Text("Add ${selectedTracks.size} Songs") },
                    icon = { Icon(Icons.Default.Add, null) }
                )
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(localSongs, key = { it.id }) { track ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (track in selectedTracks) selectedTracks.remove(track)
                            else selectedTracks.add(track)
                        }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = track in selectedTracks,
                        onCheckedChange = {
                            if (it) selectedTracks.add(track) else selectedTracks.remove(track)
                        }
                    )
                    TrackListItem(track = track, onTrackClicked = {}, isClickable = false)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFromSearchDialogContent(
    viewModel: PlaylistDetailViewModel,
    onDismiss: () -> Unit,
    onAddSongs: (List<Track>) -> Unit
) {
    val searchResults by viewModel.addSongsSearchResults.collectAsState()
    val isSearching by viewModel.isSearchingForSongs.collectAsState()
    val selectedTracks = remember { mutableStateListOf<Track>() }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            delay(300)
            viewModel.searchForSongsToAdd(searchQuery)
        } else {
            viewModel.clearSongSearchResults()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearSongSearchResults() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Online") },
                navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close") } }
            )
        },
        floatingActionButton = {
            if (selectedTracks.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {onAddSongs(selectedTracks.toList())
                        ; onDismiss() },
                    text = { Text("Add ${selectedTracks.size} Songs") },
                    icon = { Icon(Icons.Default.Add, null) }
                )
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search for songs or artists") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, "Clear") }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            if (isSearching) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(searchResults, key = { it.id }) { track ->
                        val isSelected = track in selectedTracks
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedTracks.remove(track)
                                    else selectedTracks.add(track)
                                }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (it) selectedTracks.add(track) else selectedTracks.remove(track)
                                }
                            )
                            TrackListItem(track = track, onTrackClicked = {}, isClickable = false)
                        }
                    }
                }
            }
        }
    }
}