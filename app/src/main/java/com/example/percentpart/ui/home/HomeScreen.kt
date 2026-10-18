package com.example.percentpart.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.percentpart.domain.CurrencyRepository
import com.example.percentpart.domain.PercentPartFormatter
import com.example.percentpart.domain.PercentageFormatter
import com.example.percentpart.model.CurrencyOption
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.model.PercentPartHistory
import com.example.percentpart.model.PercentPartResult
import com.example.percentpart.model.PercentPartStatus
import com.example.percentpart.model.PercentPartUiState
import com.example.percentpart.ui.PercentPartViewModel
import com.example.percentpart.util.HapticHelper
import com.example.percentpart.util.ShareCopyHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: PercentPartUiState,
    onPartChanged: (String) -> Unit,
    onWholeChanged: (String) -> Unit,
    onInputTypeChanged: (PercentInputType) -> Unit,
    onCurrencyChanged: (CurrencyOption) -> Unit,
    onCustomCurrencyUpdate: (String, Int) -> Unit,
    onQuickPartSelected: (String) -> Unit,
    onQuickWholeSelected: (String) -> Unit,
    onCommonExampleSelected: (String, String) -> Unit,
    onCalculateClicked: () -> Unit,
    onSwapClicked: () -> Unit,
    onResetClicked: () -> Unit,
    onSaveClicked: () -> Boolean,
    onRestoreHistoryItem: (PercentPartHistory) -> Unit,
    onNavigateToHistory: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val hapticsEnabled = uiState.settings.hapticFeedback
    val animationsEnabled = uiState.settings.animationsEnabled
    var currencyMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "PercentPart",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Find what percentage one value represents of another.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Input Type Selector (NUMBER / CURRENCY)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = uiState.inputType == PercentInputType.NUMBER,
                onClick = {
                    HapticHelper.performClickHaptic(view, hapticsEnabled)
                    onInputTypeChanged(PercentInputType.NUMBER)
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("NUMBER", fontWeight = FontWeight.SemiBold)
            }
            SegmentedButton(
                selected = uiState.inputType == PercentInputType.CURRENCY,
                onClick = {
                    HapticHelper.performClickHaptic(view, hapticsEnabled)
                    onInputTypeChanged(PercentInputType.CURRENCY)
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("CURRENCY", fontWeight = FontWeight.SemiBold)
            }
        }

        // Currency Selector (only when CURRENCY mode is active)
        if (uiState.inputType == PercentInputType.CURRENCY) {
            val activeCurrency = uiState.currency ?: CurrencyRepository.USD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CURRENCY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { currencyMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${activeCurrency.code} — ${activeCurrency.name} (${activeCurrency.symbol})",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Select currency")
                            }
                        }

                        DropdownMenu(
                            expanded = currencyMenuExpanded,
                            onDismissRequest = { currencyMenuExpanded = false }
                        ) {
                            CurrencyRepository.supportedCurrencies.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${option.code} — ${option.name} (${option.symbol})")
                                    },
                                    onClick = {
                                        HapticHelper.performClickHaptic(view, hapticsEnabled)
                                        val resolved = if (option.code == CurrencyRepository.CUSTOM_CODE) {
                                            CurrencyRepository.createCustomCurrency(
                                                symbol = uiState.settings.customCurrencySymbol,
                                                decimalPlaces = uiState.settings.customCurrencyDecimalPlaces
                                            )
                                        } else {
                                            option
                                        }
                                        onCurrencyChanged(resolved)
                                        currencyMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (activeCurrency.code == CurrencyRepository.CUSTOM_CODE) {
                        var symbolInput by remember(activeCurrency.symbol) {
                            mutableStateOf(activeCurrency.symbol)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = symbolInput,
                                onValueChange = {
                                    symbolInput = it.take(5)
                                    onCustomCurrencyUpdate(symbolInput, activeCurrency.decimalPlaces)
                                },
                                label = { Text("Symbol") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(1.4f)) {
                                Text(
                                    text = "Decimals (0–4)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    (0..4).forEach { dec ->
                                        FilterChip(
                                            selected = activeCurrency.decimalPlaces == dec,
                                            onClick = {
                                                onCustomCurrencyUpdate(symbolInput, dec)
                                            },
                                            label = { Text("$dec") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Inputs Card: PART VALUE -> SWAP -> WHOLE VALUE -> CALCULATE PERCENTAGE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PART VALUE
                Text(
                    text = "PART VALUE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = uiState.partValueInput,
                    onValueChange = onPartChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Part value, ${uiState.partValueInput}."
                        },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    prefix = if (uiState.inputType == PercentInputType.CURRENCY && uiState.currency != null) {
                        {
                            Text(
                                text = "${uiState.currency.symbol} ",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    placeholder = { Text("25") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Quick Part Values
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PercentPartViewModel.QUICK_PART_VALUES.forEach { preset ->
                        AssistChip(
                            onClick = {
                                HapticHelper.performClickHaptic(view, hapticsEnabled)
                                onQuickPartSelected(preset)
                            },
                            label = { Text(preset) }
                        )
                    }
                }

                // Divider + Flow Arrow + SWAP Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "↓  OUT OF WHOLE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = {
                            HapticHelper.performActionHaptic(view, hapticsEnabled)
                            onSwapClicked()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.semantics {
                            contentDescription = "Swap part and whole values."
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SWAP", fontWeight = FontWeight.Bold)
                    }
                }

                // WHOLE VALUE
                Text(
                    text = "WHOLE VALUE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = uiState.wholeValueInput,
                    onValueChange = onWholeChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Whole value, ${uiState.wholeValueInput}."
                        },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    prefix = if (uiState.inputType == PercentInputType.CURRENCY && uiState.currency != null) {
                        {
                            Text(
                                text = "${uiState.currency.symbol} ",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    placeholder = { Text("100") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Quick Whole Values
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    PercentPartViewModel.QUICK_WHOLE_VALUES.forEach { preset ->
                        AssistChip(
                            onClick = {
                                HapticHelper.performClickHaptic(view, hapticsEnabled)
                                onQuickWholeSelected(preset)
                            },
                            label = { Text(preset) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // CALCULATE PERCENTAGE Primary Action
                Button(
                    onClick = {
                        HapticHelper.performActionHaptic(view, hapticsEnabled)
                        onCalculateClicked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "CALCULATE PERCENTAGE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Validation Error Banner (for non-zero-whole validation issues like negative or excess decimals)
        if (uiState.errorMessage != null && uiState.result?.status != PercentPartStatus.UNDEFINED) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Main Result Card
        if (uiState.result != null) {
            ResultCard(
                result = uiState.result,
                errorMessage = uiState.errorMessage,
                animationsEnabled = animationsEnabled,
                onSave = {
                    HapticHelper.performActionHaptic(view, hapticsEnabled)
                    val saved = onSaveClicked()
                    if (saved) {
                        onShowSnackbar("Calculation saved to history")
                    } else {
                        onShowSnackbar("Already saved to recent history")
                    }
                },
                onCopy = {
                    HapticHelper.performClickHaptic(view, hapticsEnabled)
                    val copyText = PercentPartFormatter.formatCopyText(uiState.result)
                    ShareCopyHelper.copyToClipboard(context, copyText)
                    onShowSnackbar("Result copied to clipboard")
                },
                onShare = {
                    HapticHelper.performClickHaptic(view, hapticsEnabled)
                    val shareText = PercentPartFormatter.formatShareText(uiState.result)
                    ShareCopyHelper.shareText(context, shareText)
                },
                onSwap = {
                    HapticHelper.performActionHaptic(view, hapticsEnabled)
                    onSwapClicked()
                },
                onReset = {
                    HapticHelper.performActionHaptic(view, hapticsEnabled)
                    onResetClicked()
                    onShowSnackbar("Reset to 25 of 100")
                }
            )
        }

        // Secondary Common Examples
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "COMMON EXAMPLES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PercentPartViewModel.COMMON_EXAMPLES.forEach { (partEx, wholeEx) ->
                        AssistChip(
                            onClick = {
                                HapticHelper.performClickHaptic(view, hapticsEnabled)
                                onCommonExampleSelected(partEx, wholeEx)
                            },
                            label = { Text("$partEx of $wholeEx") }
                        )
                    }
                }
            }
        }

        // Recent Calculations Preview
        if (uiState.history.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECENT CALCULATIONS",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = onNavigateToHistory) {
                            Text("View All (${uiState.history.size})")
                        }
                    }

                    uiState.history.take(3).forEach { historyItem ->
                        val partText = PercentPartFormatter.formatHistoryPartValue(historyItem)
                        val wholeText = PercentPartFormatter.formatHistoryWholeValue(historyItem)
                        val pctText = PercentPartFormatter.formatHistoryPercentage(historyItem)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    HapticHelper.performClickHaptic(view, hapticsEnabled)
                                    onRestoreHistoryItem(historyItem)
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$partText / $wholeText",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = pctText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultCard(
    result: PercentPartResult,
    errorMessage: String?,
    animationsEnabled: Boolean,
    onSave: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onSwap: () -> Unit,
    onReset: () -> Unit
) {
    val partFormatted = PercentPartFormatter.formatValueForResult(
        value = result.partValue,
        minorUnits = result.partMinorUnits,
        inputType = result.inputType,
        currency = result.currency
    )
    val wholeFormatted = PercentPartFormatter.formatValueForResult(
        value = result.wholeValue,
        minorUnits = result.wholeMinorUnits,
        inputType = result.inputType,
        currency = result.currency
    )
    val percentageFormatted = PercentPartFormatter.formatPercentage(result.percentage)
    val remainingLabel = PercentPartFormatter.formatRemainingLabel(result)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (result.status == PercentPartStatus.UNDEFINED) {
                // Zero Whole State
                Text(
                    text = "—",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Percentage unavailable",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = errorMessage ?: "Whole value cannot be zero.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "Part: $partFormatted   •   Whole: $wholeFormatted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            } else {
                // Valid Calculation Result
                val percentageNumberOnly = PercentageFormatter.formatNumberOnly(result.percentage)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics {
                        contentDescription = "$percentageNumberOnly percent of the whole."
                    }
                ) {
                    if (animationsEnabled) {
                        AnimatedContent(
                            targetState = percentageFormatted,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(250)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                            },
                            label = "percentageAnimation"
                        ) { animatedPct ->
                            Text(
                                text = animatedPct,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = percentageFormatted,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "OF THE WHOLE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }

                // Part of Whole summary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = partFormatted,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "PART",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "of",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = wholeFormatted,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "WHOLE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Calculation Flow Visual: 25 PART -> 100 WHOLE -> 25%
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "$partFormatted PART",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "↓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$wholeFormatted WHOLE",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "↓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = percentageFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Percentage Bar & Remaining / Part Exceeds Whole Status
                if (result.status == PercentPartStatus.NORMAL) {
                    val targetProgress = PercentageFormatter.toProgressFraction(result.percentage)
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(durationMillis = if (animationsEnabled) 300 else 0),
                        label = "progressAnimation"
                    )
                    val remainingNumberOnly = PercentageFormatter.formatNumberOnly(result.remainingPercentage)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = "$remainingNumberOnly percent remaining."
                            },
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$percentageFormatted OF WHOLE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${PercentPartFormatter.formatPercentage(result.remainingPercentage)} REMAINING",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                } else if (result.status == PercentPartStatus.PART_EXCEEDS_WHOLE) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PART EXCEEDS WHOLE • Part exceeds the whole.",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = remainingLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
            )

            // Result Action Buttons: SAVE, COPY, SHARE, SWAP, RESET
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result.status != PercentPartStatus.UNDEFINED) {
                    FilledTonalButton(
                        onClick = onSave,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE")
                    }

                    FilledTonalButton(
                        onClick = onCopy,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COPY")
                    }

                    FilledTonalButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SHARE")
                    }
                }

                FilledTonalButton(
                    onClick = onSwap,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.semantics {
                        contentDescription = "Swap part and whole values."
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SwapVert,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SWAP")
                }

                FilledTonalButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET")
                }
            }
        }
    }
}
