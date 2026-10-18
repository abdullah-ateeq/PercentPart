package com.example.percentpart.domain

import com.example.percentpart.model.CurrencyOption
import java.math.BigDecimal
import java.math.RoundingMode

object MoneyFormatter {
    private val SYMBOLS_REQUIRING_SPACE = setOf("Rs.", "AED", "SAR")

    fun format(minorUnits: Long, currency: CurrencyOption): String {
        val isNegative = minorUnits < 0
        val bd = BigDecimal.valueOf(minorUnits, currency.decimalPlaces).abs()
        val scaled = bd.setScale(currency.decimalPlaces, RoundingMode.HALF_UP)
        val plain = scaled.toPlainString()
        val parts = plain.split('.')
        val integerPart = formatIntegerWithCommas(parts[0])
        val numberFormatted = if (currency.decimalPlaces > 0) {
            val fracPart = if (parts.size > 1) {
                parts[1].padEnd(currency.decimalPlaces, '0')
            } else {
                "0".repeat(currency.decimalPlaces)
            }
            "$integerPart.$fracPart"
        } else {
            integerPart
        }

        val separator = if (needsSpaceAfterSymbol(currency.symbol)) " " else ""
        val sign = if (isNegative) "-" else ""
        return "$sign${currency.symbol}$separator$numberFormatted"
    }

    fun formatBigDecimal(value: BigDecimal, currency: CurrencyOption): String {
        val minorUnits = MoneyParser.bigDecimalToMinorUnits(value, currency)
        if (minorUnits != null) {
            return format(minorUnits, currency)
        }
        val isNegative = value < BigDecimal.ZERO
        val scaled = value.abs().setScale(currency.decimalPlaces, RoundingMode.HALF_UP)
        val plain = scaled.toPlainString()
        val parts = plain.split('.')
        val integerPart = formatIntegerWithCommas(parts[0])
        val numberFormatted = if (currency.decimalPlaces > 0 && parts.size > 1) {
            "$integerPart.${parts[1]}"
        } else {
            integerPart
        }
        val separator = if (needsSpaceAfterSymbol(currency.symbol)) " " else ""
        val sign = if (isNegative) "-" else ""
        return "$sign${currency.symbol}$separator$numberFormatted"
    }

    private fun needsSpaceAfterSymbol(symbol: String): Boolean {
        if (symbol in SYMBOLS_REQUIRING_SPACE) return true
        // If symbol ends with a letter or period (e.g. "Rs.", "CHF", "AED"), add a space
        val lastChar = symbol.lastOrNull() ?: return false
        return lastChar.isLetter() || lastChar == '.'
    }

    fun formatIntegerWithCommas(digits: String): String {
        val clean = digits.trimStart('0').ifEmpty { "0" }
        if (clean.length <= 3) return clean
        val sb = StringBuilder()
        var count = 0
        for (i in clean.length - 1 downTo 0) {
            sb.append(clean[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(',')
            }
        }
        return sb.reverse().toString()
    }
}
