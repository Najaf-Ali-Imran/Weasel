package com.example.weasel.ux

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

/**
 * A reusable confirmation dialog for actions like deleting playlists.
 *
 * @param title The title of the dialog.
 * @param text The descriptive text explaining the action.
 * @param onConfirm The lambda to be executed when the user confirms the action.
 * @param onDismiss The lambda to be executed when the dialog is dismissed.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}