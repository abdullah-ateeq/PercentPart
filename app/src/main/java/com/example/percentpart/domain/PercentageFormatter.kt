package com.example.percentpart.domain

import java.math.BigDecimal
import java.math.RoundingMode

object PercentageFormatter {
    const val DISPLAY_SCALE = 2
    const val UNDEFINED_SYMBOL = "—"

    fun format(percentage: BigDecimal?): String {
        if (percentage == null) return UNDEFINED_SYMBOL
        val rounded = roundForDisplay(percentage)
        return "${rounded.toPlainString()}%"
    }

    fun formatNumberOnly(percentage: BigDecimal?): String {
        if (percentage == null) return UNDEFINED_SYMBOL
        return roundForDisplay(percentage).toPlainString()
    }

    fun roundForDisplay(value: BigDecimal): BigDecimal {
        val scaled = value.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
        val stripped = scaled.stripTrailingZeros()
        return if (stripped.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal.ZERO
        } else {
            stripped
        }
    }

    fun toProgressFraction(percentage: BigDecimal?): Float {
        if (percentage == null) return 0f
        if (percentage <= BigDecimal.ZERO) return 0f
        val hundred = BigDecimal("100")
        if (percentage >= hundred) return 1f
        return percentage
            .divide(hundred, 4, RoundingMode.HALF_UP)
            .toFloat()
            .coerceIn(0f, 1f)
    }
}
