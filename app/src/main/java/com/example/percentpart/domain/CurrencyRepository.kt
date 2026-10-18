package com.example.percentpart.domain

import com.example.percentpart.model.CurrencyOption

object CurrencyRepository {
    const val CUSTOM_CODE = "OTHER"

    val USD = CurrencyOption(code = "USD", symbol = "$", name = "US Dollar", decimalPlaces = 2)
    val EUR = CurrencyOption(code = "EUR", symbol = "€", name = "Euro", decimalPlaces = 2)
    val GBP = CurrencyOption(code = "GBP", symbol = "£", name = "British Pound", decimalPlaces = 2)
    val PKR = CurrencyOption(code = "PKR", symbol = "Rs.", name = "Pakistani Rupee", decimalPlaces = 2)
    val AED = CurrencyOption(code = "AED", symbol = "AED", name = "UAE Dirham", decimalPlaces = 2)
    val SAR = CurrencyOption(code = "SAR", symbol = "SAR", name = "Saudi Riyal", decimalPlaces = 2)
    val CAD = CurrencyOption(code = "CAD", symbol = "CA$", name = "Canadian Dollar", decimalPlaces = 2)
    val AUD = CurrencyOption(code = "AUD", symbol = "A$", name = "Australian Dollar", decimalPlaces = 2)
    val INR = CurrencyOption(code = "INR", symbol = "₹", name = "Indian Rupee", decimalPlaces = 2)
    val JPY = CurrencyOption(code = "JPY", symbol = "¥", name = "Japanese Yen", decimalPlaces = 0)
    val CNY = CurrencyOption(code = "CNY", symbol = "¥", name = "Chinese Yuan", decimalPlaces = 2)
    val OTHER = CurrencyOption(code = CUSTOM_CODE, symbol = "¤", name = "Other", decimalPlaces = 2)

    val supportedCurrencies: List<CurrencyOption> = listOf(
        USD,
        EUR,
        GBP,
        PKR,
        AED,
        SAR,
        CAD,
        AUD,
        INR,
        JPY,
        CNY,
        OTHER
    )

    fun getByCode(
        code: String?,
        customSymbol: String = "¤",
        customDecimalPlaces: Int = 2
    ): CurrencyOption {
        if (code == null) return USD
        if (code.equals(CUSTOM_CODE, ignoreCase = true)) {
            return createCustomCurrency(
                symbol = customSymbol,
                decimalPlaces = customDecimalPlaces
            )
        }
        return supportedCurrencies.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
    }

    fun createCustomCurrency(
        symbol: String,
        decimalPlaces: Int,
        code: String = CUSTOM_CODE,
        name: String = "Other"
    ): CurrencyOption {
        val cleanSymbol = symbol.trim().ifEmpty { "¤" }
        val clampedDecimals = decimalPlaces.coerceIn(0, 4)
        return CurrencyOption(
            code = code,
            symbol = cleanSymbol,
            name = name,
            decimalPlaces = clampedDecimals
        )
    }

    fun isValidCustomDecimalPlaces(decimalPlaces: Int): Boolean {
        return decimalPlaces in 0..4
    }
}
