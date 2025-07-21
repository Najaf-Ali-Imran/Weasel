package com.example.weasel.ux

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weasel.MainActivity
import com.example.weasel.data.Track
import com.example.weasel.repository.ThemeSetting
import com.example.weasel.viewmodel.LibraryViewModel
import com.example.weasel.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    libraryViewModel: LibraryViewModel,
    onNavigateUp: () -> Unit,
    onTrackClick: (Track) -> Unit,
    contentPadding: PaddingValues
) {
    val history by libraryViewModel.history.collectAsState()
    var isHistoryExpanded by remember { mutableStateOf(false) }
    var isGeneralExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val themeViewModel: ThemeViewModel = viewModel(factory = (context as MainActivity).viewModelFactory)
    val currentTheme by themeViewModel.theme.collectAsState()
    val isNavBarTranslucent by themeViewModel.isNavBarTranslucent.collectAsState()

    var showRestartDialog by remember { mutableStateOf(false) }
    var restartDialogText by remember { mutableStateOf("") }

    val bugReportUrl = "https://docs.google.com/forms/d/e/1FAIpQLSewvwCjbEZO0s9ho6c4oUfT0cIMmhdxpVRPB90WK3P9xea9-g/viewform?usp=dialog"
    val reviewUrl = "https://docs.google.com/forms/d/e/1FAIpQLSdPg8zGwYKbRFm8zZivIL45xQ0u5MY4OtGwKdDZlgG0sHpJYA/viewform?usp=dialog"
    val suggestionsUrl = "https://docs.google.com/forms/d/e/1FAIpQLSfc2wOSUGstH5CasPzXcyCcLEpHbAsE-g16wNCnsduqGTZVHA/viewform?usp=dialog"

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun restartApplication() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        (context as? Activity)?.finish()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("Restart Required") },
            text = { Text(restartDialogText) },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    restartApplication()
                }) {
                    Text("Restart Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Later")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(
                    title = "General",
                    icon = Icons.Default.Tune,
                    isExpanded = isGeneralExpanded,
                    onExpandClick = { isGeneralExpanded = !isGeneralExpanded }
                ) {
                    AnimatedVisibility(
                        visible = isGeneralExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column {
                                Text("Theme", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ThemeButton("Light", currentTheme == ThemeSetting.LIGHT, Modifier.weight(1f)) {
                                        themeViewModel.setTheme(ThemeSetting.LIGHT)
                                    }
                                    ThemeButton("Dark", currentTheme == ThemeSetting.DARK, Modifier.weight(1f)) {
                                        themeViewModel.setTheme(ThemeSetting.DARK)
                                    }
                                    ThemeButton("System", currentTheme == ThemeSetting.SYSTEM, Modifier.weight(1f)) {
                                        themeViewModel.setTheme(ThemeSetting.SYSTEM)
                                    }
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        themeViewModel.setNavBarTranslucency(!isNavBarTranslucent)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Translucent Nav Bar",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = isNavBarTranslucent,
                                    onCheckedChange = {
                                        themeViewModel.setNavBarTranslucency(it)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsSection(
                    title = "History",
                    icon = Icons.Default.History,
                    isExpanded = isHistoryExpanded,
                    onExpandClick = { isHistoryExpanded = !isHistoryExpanded }
                ) {
                    AnimatedVisibility(
                        visible = isHistoryExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            if (history.isEmpty()) {
                                Text("No listening history yet", modifier = Modifier.padding(16.dp))
                            } else {
                                history.take(10).forEach { historyEntry ->
                                    TrackListItem(track = historyEntry.track, onTrackClicked = onTrackClick)
                                }
                            }
                        }
                    }
                }
            }

            item { SettingsLinkSection("Report a Bug", Icons.Default.BugReport) { openUrl(bugReportUrl) } }
            item { SettingsLinkSection("Review Weasel", Icons.Default.RateReview) { openUrl(reviewUrl) } }
            item { SettingsLinkSection("Suggest a Feature", Icons.Default.Lightbulb) { openUrl(suggestionsUrl) } }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandClick)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle"
                )
            }
            content()
        }
    }
}

@Composable
fun SettingsLinkSection(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ThemeButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) else ButtonDefaults.outlinedButtonColors(),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(text)
    }
}