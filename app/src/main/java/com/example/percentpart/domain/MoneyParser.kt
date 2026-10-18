package com.example.percentpart.domain

import com.example.percentpart.model.CurrencyOption
import java.math.BigDecimal
import java.math.RoundingMode

object MoneyParser {
    fun stripCurrencyDecorations(raw: String, currency: CurrencyOption? = null): String {
        var text = raw.trim()
        if (currency != null) {
            if (currency.symbol.isNotEmpty() && text.startsWith(currency.symbol)) {
                text = text.removePrefix(currency.symbol).trim()
            } else if (currency.code.isNotEmpty() && text.startsWith(currency.code, ignoreCase = true)) {
                text = text.substring(currency.code.length).trim()
            }
        }
        // Also strip known prefixes if present
        val knownPrefixes = listOf("CA$", "A$", "Rs.", "AED", "SAR", "$", "€", "£", "₹", "¥", "¤")
        for (prefix in knownPrefixes) {
            if (text.startsWith(prefix)) {
                text = text.removePrefix(prefix).trim()
                break
            }
        }
        return text.replace(",", "").replace(" ", "")
    }

    fun countDecimalPlaces(raw: String, currency: CurrencyOption? = null): Int {
        val cleaned = stripCurrencyDecorations(raw, currency)
        val dotIndex = cleaned.indexOf('.')
        if (dotIndex < 0) return 0
        return cleaned.length - dotIndex - 1
    }

    fun parseToMinorUnits(raw: String, currency: CurrencyOption): Long? {
        val cleaned = stripCurrencyDecorations(raw, currency)
        if (!NumberParser.isValidSyntax(cleaned)) return null
        val decimalPlaces = countDecimalPlaces(cleaned, currency)
        if (decimalPlaces > currency.decimalPlaces) return null
        return try {
            val bd = BigDecimal(cleaned)
            bigDecimalToMinorUnits(bd, currency)
        } catch (_: Exception) {
            null
        }
    }

    fun bigDecimalToMinorUnits(value: BigDecimal, currency: CurrencyOption): Long? {
        return try {
            val scaled = value.setScale(currency.decimalPlaces, RoundingMode.HALF_UP)
            scaled.movePointRight(currency.decimalPlaces).longValueExact()
        } catch (_: ArithmeticException) {
            null
        }
    }

    fun minorUnitsToBigDecimal(minorUnits: Long, currency: CurrencyOption): BigDecimal {
        return BigDecimal.valueOf(minorUnits, currency.decimalPlaces)
    }
}
