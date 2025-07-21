package com.example.weasel.ux

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BetaDisclaimerDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissing by clicking outside */ },
        icon = { Icon(Icons.Default.Info, contentDescription = "Information") },
        title = { Text(text = "v0.1.2 Changelogs") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Welcome to the new and improved Weasel! Here are the major changes:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(" Performance/Bugs", fontWeight = FontWeight.Bold)
                Text("• Improved Startup Screen.")
                Text("• Fixed Android 15 local play issue.")
                Spacer(modifier = Modifier.height(4.dp))

                Text("  New Features ", fontWeight = FontWeight.Bold)
                Text("•Light & Dark theme support.")
                Text("• Setting to toggle a translucent or solid navigation bar.")
                Text("• Added real-time search suggestions (autocomplete).")
                Text("• Improved Playlist and Track info.")
                Spacer(modifier = Modifier.height(4.dp))

            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Continue")
            }
        }
    )
}