package com.example.percentpart.domain

import java.math.BigDecimal

object NumberParser {
    const val MAX_DECIMAL_PLACES = 12
    val MAX_SUPPORTED_VALUE = BigDecimal("999999999999999999999999999999") // 10^30 - 1

    fun normalizeInput(raw: String): String {
        return raw.trim().replace(",", "").replace(" ", "")
    }

    fun isValidSyntax(raw: String): Boolean {
        val cleaned = normalizeInput(raw)
        if (cleaned.isEmpty()) return false
        if (cleaned == "." || cleaned == "-" || cleaned == "+" || cleaned == "-.") return false
        // Standard plain decimal syntax (optional leading sign for negative detection, digits, optional single decimal point)
        val regex = Regex("^[+-]?(\\d+(\\.\\d*)?|\\.\\d+)$")
        return regex.matches(cleaned)
    }

    fun countDecimalPlaces(raw: String): Int {
        val cleaned = normalizeInput(raw)
        val dotIndex = cleaned.indexOf('.')
        if (dotIndex < 0) return 0
        return cleaned.length - dotIndex - 1
    }

    fun parse(raw: String): BigDecimal? {
        if (!isValidSyntax(raw)) return null
        val cleaned = normalizeInput(raw)
        return try {
            BigDecimal(cleaned)
        } catch (_: NumberFormatException) {
            null
        }
    }

    fun exceedsMaxValue(value: BigDecimal): Boolean {
        return value.abs() > MAX_SUPPORTED_VALUE
    }
}
