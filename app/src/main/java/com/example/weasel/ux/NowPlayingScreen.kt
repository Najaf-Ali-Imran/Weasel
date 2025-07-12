package com.example.weasel.ux

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                    fontSize = 16.sp,
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

            Spacer(modifier = Modifier.height(10.dp))

            AsyncImage(
                model = highQualityThumbnailUrl,
                contentDescription = "Album art",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = track.title,
                    color = AppText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = track.artist,
                    color = AppTextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatTime(duration),
                        color = AppTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffleEnabled) AppOrange else AppTextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.skipToPrevious() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = AppText,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (isPlaying) viewModel.pause() else viewModel.play()
                    },
                    modifier = Modifier.size(72.dp),
                    enabled = !isBuffering
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(AppOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.skipToNext() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = AppText,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleRepeat() },
                    modifier = Modifier.size(48.dp)
                ) {
                    val (icon, tint) = when (repeatMode) {
                        // The Player object is now recognized because of the import.
                        Player.REPEAT_MODE_ALL -> Icons.Default.Repeat to AppOrange
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne to AppOrange
                        else -> Icons.Default.Repeat to AppTextSecondary
                    }
                    Icon(
                        icon,
                        contentDescription = "Repeat",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
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