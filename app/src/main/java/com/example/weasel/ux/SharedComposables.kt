package com.example.weasel.ux

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.weasel.R
import com.example.weasel.ui.resources.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onSettingsClick: () -> Unit,
    downloadQueueSize: Int,
    onDownloadQueueClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = AppIcons.Person),
            contentDescription = "Profile",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* TODO: Profile */ }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))

        if (downloadQueueSize > 0) {
            BadgedBox(
                badge = {
                    Badge { Text("$downloadQueueSize") }
                }
            ) {
                IconButton(onClick = onDownloadQueueClick) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_download_queue),
                        contentDescription = "Download Queue",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        IconButton(onClick = onSettingsClick) {
            Image(
                painter = painterResource(id = AppIcons.Settings),
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp)
            )
        }
    }
    fun Modifier.clickableWithZoom(onClick: () -> Unit) = composed {
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f)

        this
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                // Set indication to null to remove the default ripple effect.
                indication = null,
                onClick = onClick
            )
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        // Wait for a press event
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        // Wait for the press to be released or for the gesture to be cancelled
                        waitForUpOrCancellation()
                        isPressed = false
                    }
                }
            }
    }
}