package com.example.percentpart.repository

import android.content.SharedPreferences
import com.example.percentpart.data.InMemoryKeyValueStore
import com.example.percentpart.data.KeyValueStore
import com.example.percentpart.data.SharedPreferencesKeyValueStore
import com.example.percentpart.model.AppSettings
import com.example.percentpart.model.AppThemeMode
import com.example.percentpart.model.PercentInputType

data class SavedCalculationInputs(
    val partValue: String,
    val wholeValue: String,
    val inputType: PercentInputType,
    val currencyCode: String,
    val customCurrencySymbol: String = "¤",
    val customCurrencyDecimalPlaces: Int = 2
)

class SettingsRepository(
    private val store: KeyValueStore = InMemoryKeyValueStore()
) {
    constructor(sharedPreferences: SharedPreferences) : this(
        SharedPreferencesKeyValueStore(sharedPreferences)
    )

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DYNAMIC_COLOR = "pref_dynamic_color"
        private const val KEY_HAPTIC_FEEDBACK = "pref_haptic_feedback"
        private const val KEY_ANIMATIONS = "pref_animations"
        private const val KEY_DEFAULT_INPUT_TYPE = "pref_default_input_type"
        private const val KEY_DEFAULT_CURRENCY = "pref_default_currency"
        private const val KEY_CUSTOM_CURRENCY_SYMBOL = "pref_custom_currency_symbol"
        private const val KEY_CUSTOM_CURRENCY_DECIMALS = "pref_custom_currency_decimals"
        private const val KEY_CONFIRM_HISTORY_DELETE = "pref_confirm_history_delete"
        private const val KEY_PRESERVE_LAST_CALC = "pref_preserve_last_calc"

        private const val KEY_LAST_PART_VALUE = "pref_last_part_value"
        private const val KEY_LAST_WHOLE_VALUE = "pref_last_whole_value"
        private const val KEY_LAST_INPUT_TYPE = "pref_last_input_type"
        private const val KEY_LAST_CURRENCY = "pref_last_currency"
        private const val KEY_HAS_SAVED_LAST_CALC = "pref_has_saved_last_calc"

        const val DEFAULT_PART_VALUE = "25"
        const val DEFAULT_WHOLE_VALUE = "100"
        const val DEFAULT_CURRENCY_CODE = "USD"
    }

    fun loadSettings(): AppSettings {
        val themeStr = store.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        val themeMode = try {
            AppThemeMode.valueOf(themeStr ?: AppThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }

        val inputTypeStr = store.getString(KEY_DEFAULT_INPUT_TYPE, PercentInputType.NUMBER.name)
        val defaultInputType = try {
            PercentInputType.valueOf(inputTypeStr ?: PercentInputType.NUMBER.name)
        } catch (_: Exception) {
            PercentInputType.NUMBER
        }

        return AppSettings(
            themeMode = themeMode,
            dynamicColor = store.getBoolean(KEY_DYNAMIC_COLOR, true),
            hapticFeedback = store.getBoolean(KEY_HAPTIC_FEEDBACK, true),
            animationsEnabled = store.getBoolean(KEY_ANIMATIONS, true),
            defaultInputType = defaultInputType,
            defaultCurrencyCode = store.getString(KEY_DEFAULT_CURRENCY, DEFAULT_CURRENCY_CODE) ?: DEFAULT_CURRENCY_CODE,
            customCurrencySymbol = store.getString(KEY_CUSTOM_CURRENCY_SYMBOL, "¤") ?: "¤",
            customCurrencyDecimalPlaces = store.getInt(KEY_CUSTOM_CURRENCY_DECIMALS, 2).coerceIn(0, 4),
            confirmHistoryDelete = store.getBoolean(KEY_CONFIRM_HISTORY_DELETE, true),
            preserveLastCalculation = store.getBoolean(KEY_PRESERVE_LAST_CALC, true)
        )
    }

    fun saveSettings(settings: AppSettings) {
        store.putString(KEY_THEME_MODE, settings.themeMode.name)
        store.putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
        store.putBoolean(KEY_HAPTIC_FEEDBACK, settings.hapticFeedback)
        store.putBoolean(KEY_ANIMATIONS, settings.animationsEnabled)
        store.putString(KEY_DEFAULT_INPUT_TYPE, settings.defaultInputType.name)
        store.putString(KEY_DEFAULT_CURRENCY, settings.defaultCurrencyCode)
        store.putString(KEY_CUSTOM_CURRENCY_SYMBOL, settings.customCurrencySymbol)
        store.putInt(KEY_CUSTOM_CURRENCY_DECIMALS, settings.customCurrencyDecimalPlaces.coerceIn(0, 4))
        store.putBoolean(KEY_CONFIRM_HISTORY_DELETE, settings.confirmHistoryDelete)
        store.putBoolean(KEY_PRESERVE_LAST_CALC, settings.preserveLastCalculation)
    }

    fun resetSettings(): AppSettings {
        val defaults = AppSettings()
        saveSettings(defaults)
        return defaults
    }

    fun saveLastCalculation(
        partValue: String,
        wholeValue: String,
        inputType: PercentInputType,
        currencyCode: String
    ) {
        store.putString(KEY_LAST_PART_VALUE, partValue)
        store.putString(KEY_LAST_WHOLE_VALUE, wholeValue)
        store.putString(KEY_LAST_INPUT_TYPE, inputType.name)
        store.putString(KEY_LAST_CURRENCY, currencyCode)
        store.putBoolean(KEY_HAS_SAVED_LAST_CALC, true)
    }

    fun loadInitialCalculationInputs(): SavedCalculationInputs {
        val settings = loadSettings()
        val hasSaved = store.getBoolean(KEY_HAS_SAVED_LAST_CALC, false)
        if (settings.preserveLastCalculation && hasSaved) {
            val part = store.getString(KEY_LAST_PART_VALUE, DEFAULT_PART_VALUE) ?: DEFAULT_PART_VALUE
            val whole = store.getString(KEY_LAST_WHOLE_VALUE, DEFAULT_WHOLE_VALUE) ?: DEFAULT_WHOLE_VALUE
            val inputTypeStr = store.getString(KEY_LAST_INPUT_TYPE, settings.defaultInputType.name)
            val inputType = try {
                PercentInputType.valueOf(inputTypeStr ?: settings.defaultInputType.name)
            } catch (_: Exception) {
                settings.defaultInputType
            }
            val currencyCode = store.getString(KEY_LAST_CURRENCY, settings.defaultCurrencyCode)
                ?: settings.defaultCurrencyCode
            return SavedCalculationInputs(
                partValue = part,
                wholeValue = whole,
                inputType = inputType,
                currencyCode = currencyCode,
                customCurrencySymbol = settings.customCurrencySymbol,
                customCurrencyDecimalPlaces = settings.customCurrencyDecimalPlaces
            )
        }

        return SavedCalculationInputs(
            partValue = DEFAULT_PART_VALUE,
            wholeValue = DEFAULT_WHOLE_VALUE,
            inputType = settings.defaultInputType,
            currencyCode = settings.defaultCurrencyCode,
            customCurrencySymbol = settings.customCurrencySymbol,
            customCurrencyDecimalPlaces = settings.customCurrencyDecimalPlaces
        )
    }
}
