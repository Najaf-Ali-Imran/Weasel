package com.example.weasel.ux

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weasel.data.Suggestion
import com.example.weasel.data.Track
import com.example.weasel.viewmodel.LibraryViewModel
import com.example.weasel.viewmodel.MusicPlayerViewModel
import com.example.weasel.viewmodel.SearchUiState
import com.example.weasel.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    libraryViewModel: LibraryViewModel,
    playerViewModel: MusicPlayerViewModel,
    onTrackClick: (Track) -> Unit,
    onSettingsClick: () -> Unit,
    contentPadding: PaddingValues,
    onDownloadQueueClick: () -> Unit,
    hasNewmessage: Boolean,
    onmessageClick: () -> Unit
) {
    val downloadQueue by libraryViewModel.downloadQueue.collectAsState()
    val focusManager = LocalFocusManager.current
    var selectedTrackForMenu by remember { mutableStateOf<Track?>(null) }
    var showTrackOptionsMenu by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    val playlists by libraryViewModel.userPlaylists.collectAsState()

    if (showTrackOptionsMenu && selectedTrackForMenu != null) {
        SearchTrackOptionsMenu(
            track = selectedTrackForMenu!!,
            onDismiss = { showTrackOptionsMenu = false },
            onDownloadClick = { libraryViewModel.downloadTrack(it) },
            onAddToPlaylistClick = { showAddToPlaylistDialog = true },
            onPlayNextClick = { playerViewModel.playTrackNext(it) },
            onAddToQueueClick = { playerViewModel.addTrackToQueue(it) },
            onLikeClick = { libraryViewModel.addTrackToLikedSongs(it) }
        )
    }

    if (showAddToPlaylistDialog && selectedTrackForMenu != null) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                libraryViewModel.addTrackToPlaylist(playlistId, selectedTrackForMenu!!)
                showAddToPlaylistDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {
        Spacer(modifier = Modifier.height(24.dp))

        TopBar(
            title = "Search",
            onSettingsClick = onSettingsClick,
            downloadQueueSize = downloadQueue.size,
            onDownloadQueueClick = onDownloadQueueClick,
            hasNewmessage = hasNewmessage,
            onmessageClick = onmessageClick
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("What do you want to listen to?", color = MaterialTheme.colorScheme.tertiary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.tertiary) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChanged("") }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = RoundedCornerShape(30.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.search()
                    focusManager.clearFocus()
                })
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            val uiState = viewModel.uiState
            val suggestions = viewModel.suggestions

            when (uiState) {
                is SearchUiState.Idle -> {
                    if (viewModel.searchQuery.isNotBlank() && suggestions.isNotEmpty()) {
                        SuggestionList(
                            suggestions = suggestions,
                            contentPadding = contentPadding,
                            onSuggestionClick = { suggestion ->
                                focusManager.clearFocus()
                                viewModel.search(suggestion)
                            }
                        )
                    } else {
                        EmptySearchState(contentPadding)
                    }
                }
                is SearchUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is SearchUiState.Success -> {
                    SearchResults(
                        tracks = uiState.tracks,
                        isLoadingMore = viewModel.isLoadingMore,
                        onTrackClick = onTrackClick,
                        onLoadMore = { viewModel.loadMoreResults() },
                        contentPadding = contentPadding,
                        onShowMenuClick = { track ->
                            selectedTrackForMenu = track
                            showTrackOptionsMenu = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    tracks: List<Track>,
    isLoadingMore: Boolean,
    onTrackClick: (Track) -> Unit,
    onLoadMore: () -> Unit,
    contentPadding: PaddingValues,
    onShowMenuClick: (Track) -> Unit
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { it != null && it >= tracks.size - 5 && !isLoadingMore }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }

    LazyColumn(state = lazyListState, contentPadding = contentPadding) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        items(tracks, key = { it.id }) { track ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackClick(track) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // The TrackListItem takes up the available space
                Box(modifier = Modifier.weight(1f)) {
                    TrackListItem(
                        track = track,
                        onTrackClicked = { onTrackClick(track) },
                        isClickable = false,
                        modifier = Modifier
                    )
                }
                // The three-dot menu button
                IconButton(onClick = { onShowMenuClick(track) }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
@Composable
private fun SuggestionList(
    suggestions: List<Suggestion>,
    contentPadding: PaddingValues,
    onSuggestionClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        items(suggestions, key = { "suggestion_${it.query}" }) { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggestionClick(suggestion.query) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Suggestion",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = suggestion.query,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
        }
    }
}


@Composable
private fun EmptySearchState(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Find your next favorite song.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
