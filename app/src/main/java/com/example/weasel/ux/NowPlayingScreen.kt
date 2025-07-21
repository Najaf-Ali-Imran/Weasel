package com.example.weasel.ux

import android.R.attr.track
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.weasel.data.Playlist
import com.example.weasel.viewmodel.LibraryViewModel
import com.example.weasel.viewmodel.MusicPlayerViewModel
import com.kmpalette.rememberPaletteState
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel,
    libraryViewModel: LibraryViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val currentTrack = viewModel.currentTrack ?: return
    val isPlaying = viewModel.isPlaying
    val currentPosition = viewModel.currentPosition
    val duration = viewModel.duration
    val isShuffleEnabled = viewModel.isShuffleEnabled
    val repeatMode = viewModel.repeatMode
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val fallbackSurface = MaterialTheme.colorScheme.surface
    val fallbackOnSurface = MaterialTheme.colorScheme.onSurface

    // Local UI State
    val paletteState = rememberPaletteState()
    val dominantColor = remember {
        Animatable(surfaceColor)
    }
    val onDominantColor = remember {
        Animatable(onSurfaceColor)
    }

    LaunchedEffect(surfaceColor) {
        dominantColor.snapTo(surfaceColor)
    }
    LaunchedEffect(onSurfaceColor) {
        onDominantColor.snapTo(onSurfaceColor)
    }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    val track = currentTrack ?: return

    val highQualityThumbnailUrl = getHighQualityYouTubeThumbnail(track.thumbnailUrl)


    LaunchedEffect(currentTrack.thumbnailUrl) {
        val imageLoader = coil.ImageLoader(context)
        val request = coil.request.ImageRequest.Builder(context)
            .data(currentTrack.thumbnailUrl)
            .build()

        val drawable = imageLoader.execute(request).drawable
        val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
        val imageBitmap = bitmap?.asImageBitmap()

        imageBitmap?.let {
            paletteState.generate(it)
        }
    }



    LaunchedEffect(paletteState.palette) {
        val newColor = paletteState.palette?.darkVibrantSwatch?.let { Color(it.rgb) }
            ?: paletteState.palette?.darkMutedSwatch?.let { Color(it.rgb) }
            ?: fallbackSurface

        val newOnColor = paletteState.palette?.darkVibrantSwatch?.let { Color(it.bodyTextColor) }
            ?: paletteState.palette?.darkMutedSwatch?.let { Color(it.bodyTextColor) }
            ?: fallbackOnSurface

        launch {
            dominantColor.animateTo(newColor, animationSpec = tween(500))
            onDominantColor.animateTo(newOnColor, animationSpec = tween(500))
        }
    }


    if (showAddToPlaylistDialog) {
        val playlists by libraryViewModel.userPlaylists.collectAsState()
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                viewModel.addCurrentTrackToPlaylist(playlistId)
                Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                showAddToPlaylistDialog = false
            }
        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(dominantColor.value, MaterialTheme.colorScheme.background),
                    endY = LocalConfiguration.current.screenHeightDp.toFloat() * 2
                )
            )
    ) {
        AsyncImage(
            model = currentTrack.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            PlayerTopBar(
                onBackClick = onBackClick,
                onAddToPlaylistClick = { showAddToPlaylistDialog = true },
                onDownloadClick = {
                    viewModel.downloadCurrentTrack(context)
                    Toast.makeText(context, "Downloading started.", Toast.LENGTH_SHORT).show()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                AsyncImage(
                    model = highQualityThumbnailUrl,
                    contentDescription = "Album art",
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                when {
                                    dragAmount < -50 -> viewModel.skipToNext()
                                    dragAmount > 50 -> viewModel.skipToPrevious()
                                }
                            }
                        },
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = currentTrack.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = onDominantColor.value,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.basicMarquee()
                )
                Text(
                    text = currentTrack.artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = onDominantColor.value.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(24.dp))

                PlayerControls(
                    viewModel = viewModel,
                    sliderColor = onDominantColor.value
                )
            }
        }
    }
}

@Composable
fun PlayerTopBar(
    onBackClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back")
        }
        Text("Now Playing", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row {
            IconButton(onClick = onAddToPlaylistClick) {
                Icon(Icons.Default.Add, contentDescription = "Add to Playlist")
            }
            IconButton(onClick = onDownloadClick) {
                Icon(Icons.Default.Download, contentDescription = "Download")
            }
        }
    }
}



@Composable
fun PlayerControls(viewModel: MusicPlayerViewModel, sliderColor: Color) {
    val currentPosition = viewModel.currentPosition
    val duration = viewModel.duration
    val isPlaying = viewModel.isPlaying
    val isShuffleEnabled = viewModel.isShuffleEnabled
    val repeatMode = viewModel.repeatMode

    val controlsColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            onValueChange = { viewModel.seekTo((it * duration).toLong()) },
            colors = SliderDefaults.colors(
                thumbColor = controlsColor,
                activeTrackColor = controlsColor,
                inactiveTrackColor = controlsColor.copy(alpha = 0.3f)
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatTime(currentPosition), style = MaterialTheme.typography.bodySmall, color = controlsColor)
            Text(formatTime(duration), style = MaterialTheme.typography.bodySmall, color = controlsColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleShuffle() }) {
                Icon(
                    Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffleEnabled) controlsColor else controlsColor.copy(alpha = 0.5f)
                )
            }
            IconButton(onClick = { viewModel.skipToPrevious() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(40.dp), tint = controlsColor)
            }
            IconButton(
                onClick = { if (isPlaying) viewModel.pause() else viewModel.play() },
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                    contentDescription = "Play/Pause",
                    tint = controlsColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(onClick = { viewModel.skipToNext() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(40.dp), tint = controlsColor)
            }
            IconButton(onClick = { viewModel.toggleRepeat() }) {
                val (icon, tint) = when (repeatMode) {
                    Player.REPEAT_MODE_ALL -> Icons.Default.Repeat to controlsColor
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne to controlsColor
                    else -> Icons.Default.Repeat to controlsColor.copy(alpha = 0.5f)
                }
                Icon(icon, contentDescription = "Repeat", tint = tint)
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    if (timeMs < 0) {
        return "--:--"
    }
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun getHighQualityYouTubeThumbnail(url: String?): String? {
    if (url.isNullOrEmpty() || !url.contains("ytimg.com/vi/")) {
        return url
    }
    val videoId = url.substringAfter("/vi/").substringBefore("/")
    return "https://i.ytimg.com/vi/$videoId/maxresdefault.jpg"
}