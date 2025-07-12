package com.example.weasel.ux

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable

@Composable
fun BetaDisclaimerDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissing by clicking outside */ },
        icon = { Icon(Icons.Default.Info, contentDescription = "Information") },
        title = { Text(text = "Welcome to the Beta!") },
        text = {
            Text(
                "This is a pre-release version of Weasel. You might encounter some bugs. " +
                        "If you find an issue, please report it via the Settings page. " +
                        "Thank you for helping me improve the app!"
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Start Listening")
            }
        }
    )
}