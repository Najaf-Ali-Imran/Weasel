package com.example.weasel.ux

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.weasel.MainActivity
import com.example.weasel.ui.theme.*
import com.example.weasel.viewmodel.LibraryViewModel
import com.example.weasel.viewmodel.MusicPlayerViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel,
    onBackClick: () -> Unit
) {
    val libraryViewModel: LibraryViewModel = viewModel(factory = (LocalContext.current as MainActivity).viewModelFactory)

    val currentTrack = viewModel.currentTrack
    val isPlaying = viewModel.isPlaying
    val currentPosition = viewModel.currentPosition
    val duration = viewModel.duration
    val isShuffleEnabled = viewModel.isShuffleEnabled
    val repeatMode = viewModel.repeatMode
    val isBuffering = viewModel.isBuffering

    val playlists by libraryViewModel.playlists.collectAsState()
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    val track = currentTrack ?: return

    val highQualityThumbnailUrl = getHighQualityYouTubeThumbnail(track.thumbnailUrl)

    // Get screen configuration for responsive design
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isCompactScreen = screenHeight < 700.dp
    val isSmallScreen = screenWidth < 400.dp

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = playlists,
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                viewModel.addCurrentTrackToPlaylist(playlistId)
                showAddToPlaylistDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppBlack,
                        AppBlack
                    )
                )
            )
    ) {
        AsyncImage(
            model = highQualityThumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(30.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.1f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isSmallScreen) 16.dp else 20.dp)
                .then(
                    if (isCompactScreen) {
                        Modifier.verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    }
                )
        ) {
            // Header with back button and controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = if (isCompactScreen) 12.dp else 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back",
                        tint = AppText,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Now Playing",
                    color = AppText,
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Row {
                    IconButton(
                        onClick = { showAddToPlaylistDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add to Playlist",
                            tint = AppOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    val context = LocalContext.current

                    IconButton(onClick = {
                        viewModel.downloadCurrentTrack(context)
                        Toast.makeText(
                            context,
                            "Downloading started.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                    }
                }
            }

            // Spacer with dynamic height
            Spacer(modifier = Modifier.height(if (isCompactScreen) 5.dp else 10.dp))

            // Album art with responsive sizing
            val albumArtSize = when {
                isCompactScreen && isSmallScreen -> 0.7f
                isCompactScreen -> 0.75f
                isSmallScreen -> 0.8f
                else -> 0.85f
            }

            AsyncImage(
                model = highQualityThumbnailUrl,
                contentDescription = "Album art",
                modifier = Modifier
                    .fillMaxWidth(albumArtSize)
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            when {
                                dragAmount < -50 -> viewModel.skipToNext()
                                dragAmount > 50 -> viewModel.skipToPrevious()
                            }
                        }
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(if (isCompactScreen) 16.dp else 32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track.title,
                    color = AppText,
                    fontSize = when {
                        isCompactScreen && isSmallScreen -> 18.sp
                        isCompactScreen -> 20.sp
                        isSmallScreen -> 22.sp
                        else -> 24.sp
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = if (isCompactScreen) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(if (isCompactScreen) 4.dp else 8.dp))
                Text(
                    text = track.artist,
                    color = AppTextSecondary,
                    fontSize = when {
                        isCompactScreen && isSmallScreen -> 13.sp
                        isCompactScreen -> 14.sp
                        isSmallScreen -> 15.sp
                        else -> 16.sp
                    },
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (isCompactScreen) 8.dp else 10.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    onValueChange = { newValue -> viewModel.seekTo((newValue * duration).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = AppOrange,
                        activeTrackColor = AppOrange,
                        inactiveTrackColor = AppTextSecondary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        color = AppTextSecondary,
                        fontSize = if (isSmallScreen) 11.sp else 12.sp
                    )
                    Text(
                        text = formatTime(duration),
                        color = AppTextSecondary,
                        fontSize = if (isSmallScreen) 11.sp else 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactScreen) 16.dp else 32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isSmallScreen) Arrangement.SpaceBetween else Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(if (isCompactScreen) 40.dp else 48.dp)
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) AppOrange else AppTextSecondary,
                        modifier = Modifier.size(if (isCompactScreen) 20.dp else 24.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.skipToPrevious() },
                    modifier = Modifier.size(if (isCompactScreen) 48.dp else 56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = AppText,
                        modifier = Modifier.size(if (isCompactScreen) 28.dp else 32.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (isPlaying) viewModel.pause() else viewModel.play()
                    },
                    modifier = Modifier.size(if (isCompactScreen) 60.dp else 72.dp),
                    enabled = !isBuffering
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompactScreen) 52.dp else 64.dp)
                            .background(AppOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(if (isCompactScreen) 24.dp else 32.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(if (isCompactScreen) 24.dp else 32.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.skipToNext() },
                    modifier = Modifier.size(if (isCompactScreen) 48.dp else 56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = AppText,
                        modifier = Modifier.size(if (isCompactScreen) 28.dp else 32.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.size(if (isCompactScreen) 40.dp else 48.dp)
                ) {
                    val (icon, tint) = when (repeatMode) {
                        Player.REPEAT_MODE_ALL -> Icons.Default.Repeat to AppOrange
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne to AppOrange
                        else -> Icons.Default.Repeat to AppTextSecondary
                    }
                    Icon(
                        icon,
                        contentDescription = "Repeat",
                        tint = tint,
                        modifier = Modifier.size(if (isCompactScreen) 20.dp else 24.dp)
                    )
                }
            }

            if (!isCompactScreen) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
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