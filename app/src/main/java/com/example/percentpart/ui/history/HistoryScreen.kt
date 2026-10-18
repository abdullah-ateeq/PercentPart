package com.example.percentpart.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.percentpart.domain.PercentPartFormatter
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.model.PercentPartHistory
import com.example.percentpart.model.PercentPartStatus
import com.example.percentpart.util.HapticHelper

@Composable
fun HistoryScreen(
    history: List<PercentPartHistory>,
    confirmDelete: Boolean,
    hapticsEnabled: Boolean,
    onSelectHistoryItem: (PercentPartHistory) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onDialogVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var pendingDeleteItem by remember { mutableStateOf<PercentPartHistory?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val anyDialogOpen = pendingDeleteItem != null || showClearAllDialog
    LaunchedEffect(anyDialogOpen) {
        onDialogVisibilityChanged(anyDialogOpen)
    }
    DisposableEffect(Unit) {
        onDispose { onDialogVisibilityChanged(false) }
    }

    if (pendingDeleteItem != null) {
        val item = pendingDeleteItem!!
        val partDisplay = PercentPartFormatter.formatHistoryPartValue(item)
        val wholeDisplay = PercentPartFormatter.formatHistoryWholeValue(item)
        AlertDialog(
            onDismissRequest = { pendingDeleteItem = null },
            title = { Text("Delete Calculation?") },
            text = {
                Text("Remove $partDisplay / $wholeDisplay (${PercentPartFormatter.formatHistoryPercentage(item)}) from recent calculations?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticHelper.performActionHaptic(view, hapticsEnabled)
                        onDeleteHistoryItem(item.id)
                        pendingDeleteItem = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all recent percentage calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticHelper.performActionHaptic(view, hapticsEnabled)
                        onClearAllHistory()
                        showClearAllDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "RECENT CALCULATIONS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Saved Calculations (${history.size}/10)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (history.isNotEmpty()) {
                OutlinedButton(
                    onClick = {
                        HapticHelper.performClickHaptic(view, hapticsEnabled)
                        showClearAllDialog = true
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved calculations yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap SAVE on the Home screen to store up to 10 recent part-of-whole calculations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = history,
                    key = { it.id }
                ) { item ->
                    val dismissState = rememberSwipeToDismissBoxState()
                    LaunchedEffect(dismissState.currentValue) {
                        val current = dismissState.currentValue
                        if (current == SwipeToDismissBoxValue.EndToStart || current == SwipeToDismissBoxValue.StartToEnd) {
                            if (confirmDelete) {
                                pendingDeleteItem = item
                                dismissState.reset()
                            } else {
                                HapticHelper.performActionHaptic(view, hapticsEnabled)
                                onDeleteHistoryItem(item.id)
                            }
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {}
                    ) {
                        HistoryItemCard(
                            item = item,
                            onTap = {
                                HapticHelper.performClickHaptic(view, hapticsEnabled)
                                onSelectHistoryItem(item)
                            },
                            onDeleteClick = {
                                HapticHelper.performClickHaptic(view, hapticsEnabled)
                                if (confirmDelete) {
                                    pendingDeleteItem = item
                                } else {
                                    onDeleteHistoryItem(item.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: PercentPartHistory,
    onTap: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val partFormatted = PercentPartFormatter.formatHistoryPartValue(item)
    val wholeFormatted = PercentPartFormatter.formatHistoryWholeValue(item)
    val percentageFormatted = PercentPartFormatter.formatHistoryPercentage(item)
    val remainingFormatted = PercentPartFormatter.formatHistoryRemaining(item)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .semantics {
                contentDescription = "$partFormatted out of $wholeFormatted equals $percentageFormatted. Tap to restore."
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (item.inputType == PercentInputType.CURRENCY) {
                                item.currencyCode ?: "CURRENCY"
                            } else {
                                "NUMBER"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.status == PercentPartStatus.PART_EXCEEDS_WHOLE) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "PART > WHOLE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$partFormatted / $wholeFormatted",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (remainingFormatted != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = remainingFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = percentageFormatted,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.semantics {
                        contentDescription = "Delete calculation $partFormatted of $wholeFormatted"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete history entry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
