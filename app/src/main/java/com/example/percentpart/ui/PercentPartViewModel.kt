package com.example.percentpart.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.percentpart.domain.CurrencyRepository
import com.example.percentpart.domain.PercentPartCalculator
import com.example.percentpart.domain.PercentPartFormatter
import com.example.percentpart.domain.PercentPartValidator
import com.example.percentpart.domain.ValidationResult
import com.example.percentpart.model.AppSettings
import com.example.percentpart.model.AppThemeMode
import com.example.percentpart.model.CalculationState
import com.example.percentpart.model.CurrencyOption
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.model.PercentPartHistory
import com.example.percentpart.model.PercentPartStatus
import com.example.percentpart.model.PercentPartUiState
import com.example.percentpart.repository.PercentPartHistoryRepository
import com.example.percentpart.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.util.UUID

class PercentPartViewModel(
    private val settingsRepository: SettingsRepository = SettingsRepository(),
    private val historyRepository: PercentPartHistoryRepository = PercentPartHistoryRepository(),
    private val clockProvider: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    companion object {
        val QUICK_PART_VALUES = listOf("10", "25", "50", "75", "100")
        val QUICK_WHOLE_VALUES = listOf("50", "100", "200", "500", "1,000")
        val COMMON_EXAMPLES = listOf(
            "25" to "100",
            "50" to "200",
            "30" to "120",
            "75" to "100"
        )
        private const val RAPID_SAVE_WINDOW_MS = 600L

        fun provideFactory(
            settingsRepository: SettingsRepository,
            historyRepository: PercentPartHistoryRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PercentPartViewModel(
                    settingsRepository = settingsRepository,
                    historyRepository = historyRepository
                ) as T
            }
        }
    }

    private var lastSavedSignature: String? = null
    private var lastSavedTimestampMs: Long = 0L

    private val _uiState: MutableStateFlow<PercentPartUiState>
    val uiState: StateFlow<PercentPartUiState>

    init {
        val loadedSettings = settingsRepository.loadSettings()
        val initialInputs = settingsRepository.loadInitialCalculationInputs()
        val initialHistory = historyRepository.loadHistory()
        val resolvedCurrency = CurrencyRepository.getByCode(
            code = initialInputs.currencyCode,
            customSymbol = loadedSettings.customCurrencySymbol,
            customDecimalPlaces = loadedSettings.customCurrencyDecimalPlaces
        )

        val baseState = PercentPartUiState(
            partValueInput = initialInputs.partValue,
            wholeValueInput = initialInputs.wholeValue,
            inputType = initialInputs.inputType,
            currency = resolvedCurrency,
            result = null,
            history = initialHistory,
            errorMessage = null,
            isCalculating = false,
            calculationState = CalculationState.Idle,
            settings = loadedSettings
        )

        _uiState = MutableStateFlow(
            evaluateCalculation(
                state = baseState,
                isExplicitAction = false,
                persistInputs = false
            )
        )
        uiState = _uiState.asStateFlow()
    }

    fun onPartValueChanged(newPart: String) {
        _uiState.update { current ->
            evaluateCalculation(
                state = current.copy(partValueInput = newPart),
                isExplicitAction = false,
                persistInputs = true
            )
        }
    }

    fun onWholeValueChanged(newWhole: String) {
        _uiState.update { current ->
            evaluateCalculation(
                state = current.copy(wholeValueInput = newWhole),
                isExplicitAction = false,
                persistInputs = true
            )
        }
    }

    fun onInputTypeChanged(newInputType: PercentInputType) {
        _uiState.update { current ->
            val activeCurrency = current.currency ?: CurrencyRepository.getByCode(
                code = current.settings.defaultCurrencyCode,
                customSymbol = current.settings.customCurrencySymbol,
                customDecimalPlaces = current.settings.customCurrencyDecimalPlaces
            )
            evaluateCalculation(
                state = current.copy(
                    inputType = newInputType,
                    currency = activeCurrency
                ),
                isExplicitAction = false,
                persistInputs = true
            )
        }
    }

    fun onCurrencyChanged(newCurrency: CurrencyOption) {
        _uiState.update { current ->
            val adjustedPart = adaptInputForCurrencySwitch(current.partValueInput, newCurrency)
            val adjustedWhole = adaptInputForCurrencySwitch(current.wholeValueInput, newCurrency)
            evaluateCalculation(
                state = current.copy(
                    partValueInput = adjustedPart,
                    wholeValueInput = adjustedWhole,
                    currency = newCurrency
                ),
                isExplicitAction = false,
                persistInputs = true
            )
        }
    }

    fun onQuickPartSelected(value: String) {
        val clean = value.replace(",", "")
        onPartValueChanged(clean)
    }

    fun onQuickWholeSelected(value: String) {
        val clean = value.replace(",", "")
        onWholeValueChanged(clean)
    }

    fun onCommonExampleSelected(part: String, whole: String) {
        _uiState.update { current ->
            evaluateCalculation(
                state = current.copy(
                    partValueInput = part.replace(",", ""),
                    wholeValueInput = whole.replace(",", "")
                ),
                isExplicitAction = true,
                persistInputs = true
            )
        }
    }

    fun calculate() {
        _uiState.update { current ->
            evaluateCalculation(
                state = current,
                isExplicitAction = true,
                persistInputs = true
            )
        }
    }

    fun swapValues() {
        _uiState.update { current ->
            val swappedState = current.copy(
                partValueInput = current.wholeValueInput,
                wholeValueInput = current.partValueInput
            )
            evaluateCalculation(
                state = swappedState,
                isExplicitAction = true,
                persistInputs = true
            )
        }
    }

    fun reset() {
        _uiState.update { current ->
            val defaultCurrency = CurrencyRepository.getByCode(
                code = current.settings.defaultCurrencyCode,
                customSymbol = current.settings.customCurrencySymbol,
                customDecimalPlaces = current.settings.customCurrencyDecimalPlaces
            )
            val resetState = current.copy(
                partValueInput = SettingsRepository.DEFAULT_PART_VALUE,
                wholeValueInput = SettingsRepository.DEFAULT_WHOLE_VALUE,
                inputType = PercentInputType.NUMBER,
                currency = defaultCurrency,
                errorMessage = null
            )
            evaluateCalculation(
                state = resetState,
                isExplicitAction = false,
                persistInputs = true
            )
        }
    }

    fun saveCurrentResultToHistory(): Boolean {
        val current = _uiState.value
        val result = current.result ?: return false
        if (result.status == PercentPartStatus.UNDEFINED || result.percentage == null) {
            return false
        }

        val now = clockProvider()
        val currencyCode = if (result.inputType == PercentInputType.CURRENCY) {
            result.currency?.code
        } else {
            null
        }
        val currencySymbol = if (result.inputType == PercentInputType.CURRENCY) {
            result.currency?.symbol
        } else {
            null
        }
        val partPlain = result.partValue.stripTrailingZeros().let {
            if (it.compareTo(BigDecimal.ZERO) == 0) "0" else it.toPlainString()
        }
        val wholePlain = result.wholeValue.stripTrailingZeros().let {
            if (it.compareTo(BigDecimal.ZERO) == 0) "0" else it.toPlainString()
        }
        val percentagePlain = result.percentage.stripTrailingZeros().let {
            if (it.compareTo(BigDecimal.ZERO) == 0) "0" else it.toPlainString()
        }
        val remainingPlain = result.remainingPercentage?.stripTrailingZeros()?.let {
            if (it.compareTo(BigDecimal.ZERO) == 0) "0" else it.toPlainString()
        }

        val signature = "$partPlain|$wholePlain|${result.inputType.name}|${currencyCode.orEmpty()}|${currencySymbol.orEmpty()}"
        if (signature == lastSavedSignature && (now - lastSavedTimestampMs) in 0..<RAPID_SAVE_WINDOW_MS) {
            return false
        }

        lastSavedSignature = signature
        lastSavedTimestampMs = now

        val historyItem = PercentPartHistory(
            id = UUID.randomUUID().toString(),
            partValue = partPlain,
            wholeValue = wholePlain,
            percentage = percentagePlain,
            remainingPercentage = remainingPlain,
            status = result.status,
            inputType = result.inputType,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            calculatedAt = now,
            currencyDecimalPlaces = result.currency?.decimalPlaces
        )

        val updatedHistory = historyRepository.saveCalculation(historyItem)
        _uiState.update { it.copy(history = updatedHistory) }
        return true
    }

    fun restoreHistoryItem(item: PercentPartHistory) {
        _uiState.update { current ->
            val restoredCurrency = if (item.inputType == PercentInputType.CURRENCY) {
                if (item.currencyCode == CurrencyRepository.CUSTOM_CODE) {
                    CurrencyRepository.createCustomCurrency(
                        symbol = item.currencySymbol ?: current.settings.customCurrencySymbol,
                        decimalPlaces = item.currencyDecimalPlaces ?: current.settings.customCurrencyDecimalPlaces
                    )
                } else {
                    CurrencyRepository.getByCode(
                        code = item.currencyCode ?: current.settings.defaultCurrencyCode,
                        customSymbol = item.currencySymbol ?: current.settings.customCurrencySymbol,
                        customDecimalPlaces = item.currencyDecimalPlaces ?: current.settings.customCurrencyDecimalPlaces
                    )
                }
            } else {
                current.currency
            }

            val restoredState = current.copy(
                partValueInput = item.partValue,
                wholeValueInput = item.wholeValue,
                inputType = item.inputType,
                currency = restoredCurrency
            )
            evaluateCalculation(
                state = restoredState,
                isExplicitAction = true,
                persistInputs = true
            )
        }
    }

    fun deleteHistoryItem(id: String) {
        val updated = historyRepository.deleteCalculation(id)
        _uiState.update { it.copy(history = updated) }
    }

    fun clearHistory() {
        val cleared = historyRepository.clearHistory()
        lastSavedSignature = null
        _uiState.update { it.copy(history = cleared) }
    }

    fun getCopyText(): String? {
        val res = _uiState.value.result ?: return null
        return PercentPartFormatter.formatCopyText(res)
    }

    fun getShareText(): String? {
        val res = _uiState.value.result ?: return null
        return PercentPartFormatter.formatShareText(res)
    }

    // Settings updates

    fun updateThemeMode(themeMode: AppThemeMode) {
        updateSettings { it.copy(themeMode = themeMode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        updateSettings { it.copy(dynamicColor = enabled) }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        updateSettings { it.copy(hapticFeedback = enabled) }
    }

    fun updateAnimationsEnabled(enabled: Boolean) {
        updateSettings { it.copy(animationsEnabled = enabled) }
    }

    fun updateDefaultInputType(inputType: PercentInputType) {
        updateSettings { it.copy(defaultInputType = inputType) }
        onInputTypeChanged(inputType)
    }

    fun updateDefaultCurrency(currencyCode: String) {
        updateSettings { it.copy(defaultCurrencyCode = currencyCode) }
        val current = _uiState.value
        val newOption = CurrencyRepository.getByCode(
            code = currencyCode,
            customSymbol = current.settings.customCurrencySymbol,
            customDecimalPlaces = current.settings.customCurrencyDecimalPlaces
        )
        onCurrencyChanged(newOption)
    }

    fun updateCustomCurrency(symbol: String, decimalPlaces: Int) {
        val clampedDecimals = decimalPlaces.coerceIn(0, 4)
        val cleanSymbol = symbol.trim().ifEmpty { "¤" }
        updateSettings {
            it.copy(
                customCurrencySymbol = cleanSymbol,
                customCurrencyDecimalPlaces = clampedDecimals
            )
        }
        val customCurrency = CurrencyRepository.createCustomCurrency(
            symbol = cleanSymbol,
            decimalPlaces = clampedDecimals
        )
        if (_uiState.value.currency?.code == CurrencyRepository.CUSTOM_CODE) {
            onCurrencyChanged(customCurrency)
        }
    }

    fun updateConfirmHistoryDelete(enabled: Boolean) {
        updateSettings { it.copy(confirmHistoryDelete = enabled) }
    }

    fun updatePreserveLastCalculation(enabled: Boolean) {
        updateSettings { it.copy(preserveLastCalculation = enabled) }
        if (enabled) {
            val s = _uiState.value
            settingsRepository.saveLastCalculation(
                partValue = s.partValueInput,
                wholeValue = s.wholeValueInput,
                inputType = s.inputType,
                currencyCode = s.currency?.code ?: s.settings.defaultCurrencyCode
            )
        }
    }

    fun resetSettings() {
        val defaults = settingsRepository.resetSettings()
        val defaultCurrency = CurrencyRepository.getByCode(
            code = defaults.defaultCurrencyCode,
            customSymbol = defaults.customCurrencySymbol,
            customDecimalPlaces = defaults.customCurrencyDecimalPlaces
        )
        _uiState.update { current ->
            evaluateCalculation(
                state = current.copy(
                    settings = defaults,
                    currency = defaultCurrency
                ),
                isExplicitAction = false,
                persistInputs = true
            )
        }
    }

    private fun updateSettings(transform: (AppSettings) -> AppSettings) {
        _uiState.update { current ->
            val updated = transform(current.settings)
            settingsRepository.saveSettings(updated)
            current.copy(settings = updated)
        }
    }

    private fun adaptInputForCurrencySwitch(input: String, currency: CurrencyOption): String {
        if (currency.decimalPlaces == 0 && input.contains('.')) {
            val parsed = try {
                BigDecimal(input.trim().replace(",", ""))
            } catch (_: Exception) {
                null
            }
            if (parsed != null && parsed.stripTrailingZeros().scale() <= 0) {
                return parsed.toBigInteger().toString()
            }
        }
        return input
    }

    private fun evaluateCalculation(
        state: PercentPartUiState,
        isExplicitAction: Boolean,
        persistInputs: Boolean
    ): PercentPartUiState {
        val validation = PercentPartValidator.validate(
            partInput = state.partValueInput,
            wholeInput = state.wholeValueInput,
            inputType = state.inputType,
            currency = state.currency
        )

        return when (validation) {
            is ValidationResult.Valid -> {
                val result = if (
                    state.inputType == PercentInputType.CURRENCY &&
                    state.currency != null &&
                    validation.partMinorUnits != null &&
                    validation.wholeMinorUnits != null
                ) {
                    PercentPartCalculator.calculateFromMinorUnits(
                        partMinorUnits = validation.partMinorUnits,
                        wholeMinorUnits = validation.wholeMinorUnits,
                        currency = state.currency
                    )
                } else {
                    PercentPartCalculator.calculate(
                        partValue = validation.partValue,
                        wholeValue = validation.wholeValue,
                        inputType = state.inputType,
                        currency = if (state.inputType == PercentInputType.CURRENCY) state.currency else null
                    )
                }

                if (persistInputs && state.settings.preserveLastCalculation) {
                    settingsRepository.saveLastCalculation(
                        partValue = state.partValueInput,
                        wholeValue = state.wholeValueInput,
                        inputType = state.inputType,
                        currencyCode = state.currency?.code ?: state.settings.defaultCurrencyCode
                    )
                }

                state.copy(
                    result = result,
                    errorMessage = null,
                    isCalculating = false,
                    calculationState = CalculationState.Calculated,
                    partMinorUnits = validation.partMinorUnits,
                    wholeMinorUnits = validation.wholeMinorUnits
                )
            }

            is ValidationResult.ZeroWhole -> {
                val undefinedResult = PercentPartCalculator.calculate(
                    partValue = validation.partValue,
                    wholeValue = validation.wholeValue,
                    inputType = state.inputType,
                    currency = if (state.inputType == PercentInputType.CURRENCY) state.currency else null
                )
                state.copy(
                    result = undefinedResult,
                    errorMessage = validation.message,
                    isCalculating = false,
                    calculationState = CalculationState.Error(validation.message),
                    partMinorUnits = validation.partMinorUnits,
                    wholeMinorUnits = validation.wholeMinorUnits
                )
            }

            is ValidationResult.Invalid -> {
                val shouldShowError = !validation.isIncomplete || isExplicitAction
                state.copy(
                    result = null,
                    errorMessage = if (shouldShowError) validation.message else null,
                    isCalculating = false,
                    calculationState = if (shouldShowError) {
                        CalculationState.Error(validation.message)
                    } else {
                        CalculationState.Idle
                    },
                    partMinorUnits = null,
                    wholeMinorUnits = null
                )
            }
        }
    }
}
