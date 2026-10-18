package com.example.percentpart.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.percentpart.domain.CurrencyRepository
import com.example.percentpart.model.AppSettings
import com.example.percentpart.model.AppThemeMode
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.util.HapticHelper

@Composable
fun SettingsScreen(
    settings: AppSettings,
    historyCount: Int,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onAnimationsChange: (Boolean) -> Unit,
    onDefaultInputTypeChange: (PercentInputType) -> Unit,
    onDefaultCurrencyChange: (String) -> Unit,
    onCustomCurrencyUpdate: (String, Int) -> Unit,
    onConfirmHistoryDeleteChange: (Boolean) -> Unit,
    onPreserveLastCalcChange: (Boolean) -> Unit,
    onClearHistory: () -> Unit,
    onResetSettings: () -> Unit,
    onDialogVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showCurrencyDropdown by remember { mutableStateOf(false) }

    val anyDialog = showClearHistoryDialog || showResetSettingsDialog
    LaunchedEffect(anyDialog) {
        onDialogVisibilityChanged(anyDialog)
    }
    DisposableEffect(Unit) {
        onDispose { onDialogVisibilityChanged(false) }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear History") },
            text = { Text("Clear all recent percentage calculations?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticHelper.performActionHaptic(view, settings.hapticFeedback)
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Restore all preferences to their original defaults?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HapticHelper.performActionHaptic(view, settings.hapticFeedback)
                        onResetSettings()
                        showResetSettingsDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        // Appearance Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Appearance & Feedback",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = settings.themeMode == mode,
                                onClick = {
                                    HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                                    onThemeModeChange(mode)
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = AppThemeMode.entries.size
                                )
                            ) {
                                Text(mode.label)
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                SettingSwitchRow(
                    title = "Dynamic Color",
                    subtitle = "Adapt palette to Android 12+ wallpaper colors",
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                        onDynamicColorChange(it)
                    }
                )

                SettingSwitchRow(
                    title = "Haptic Feedback",
                    subtitle = "Tactile feedback when tapping buttons and presets",
                    checked = settings.hapticFeedback,
                    onCheckedChange = {
                        HapticHelper.performClickHaptic(view, it)
                        onHapticsChange(it)
                    }
                )

                SettingSwitchRow(
                    title = "Animations",
                    subtitle = "Smooth percentage result and progress bar transitions",
                    checked = settings.animationsEnabled,
                    onCheckedChange = {
                        HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                        onAnimationsChange(it)
                    }
                )
            }
        }

        // Calculation Defaults Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Calculation Defaults",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Default Input Type",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        PercentInputType.entries.forEachIndexed { index, inputType ->
                            SegmentedButton(
                                selected = settings.defaultInputType == inputType,
                                onClick = {
                                    HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                                    onDefaultInputTypeChange(inputType)
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = PercentInputType.entries.size
                                )
                            ) {
                                Text(if (inputType == PercentInputType.NUMBER) "Number" else "Currency")
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Default Currency",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val selectedCurrency = CurrencyRepository.getByCode(
                        code = settings.defaultCurrencyCode,
                        customSymbol = settings.customCurrencySymbol,
                        customDecimalPlaces = settings.customCurrencyDecimalPlaces
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showCurrencyDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${selectedCurrency.code} — ${selectedCurrency.name} (${selectedCurrency.symbol})")
                                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = showCurrencyDropdown,
                            onDismissRequest = { showCurrencyDropdown = false }
                        ) {
                            CurrencyRepository.supportedCurrencies.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text("${option.code} — ${option.name} (${option.symbol})") },
                                    onClick = {
                                        HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                                        onDefaultCurrencyChange(option.code)
                                        showCurrencyDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (settings.defaultCurrencyCode == CurrencyRepository.CUSTOM_CODE) {
                    var symbolDraft by remember(settings.customCurrencySymbol) {
                        mutableStateOf(settings.customCurrencySymbol)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = symbolDraft,
                            onValueChange = {
                                symbolDraft = it.take(5)
                                onCustomCurrencyUpdate(symbolDraft, settings.customCurrencyDecimalPlaces)
                            },
                            label = { Text("Custom Currency Symbol") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Custom Decimal Places (0–4)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (0..4).forEach { decimals ->
                                FilterChip(
                                    selected = settings.customCurrencyDecimalPlaces == decimals,
                                    onClick = {
                                        onCustomCurrencyUpdate(symbolDraft, decimals)
                                    },
                                    label = { Text("$decimals") }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                SettingSwitchRow(
                    title = "Preserve Last Calculation",
                    subtitle = "Restore your last part, whole, input type, and currency on launch",
                    checked = settings.preserveLastCalculation,
                    onCheckedChange = {
                        HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                        onPreserveLastCalcChange(it)
                    }
                )

                SettingSwitchRow(
                    title = "Confirm History Delete",
                    subtitle = "Ask for confirmation before deleting an item from history",
                    checked = settings.confirmHistoryDelete,
                    onCheckedChange = {
                        HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                        onConfirmHistoryDeleteChange(it)
                    }
                )
            }
        }

        // Data & Reset Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Data & Reset",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                        showClearHistoryDialog = true
                    },
                    enabled = historyCount > 0,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear History ($historyCount)")
                }

                OutlinedButton(
                    onClick = {
                        HapticHelper.performClickHaptic(view, settings.hapticFeedback)
                        showResetSettingsDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
