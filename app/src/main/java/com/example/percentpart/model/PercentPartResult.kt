package com.example.percentpart.model

import java.math.BigDecimal

data class PercentPartResult(
    val partValue: BigDecimal,
    val wholeValue: BigDecimal,
    val percentage: BigDecimal?,
    val remainingPercentage: BigDecimal?,
    val inputType: PercentInputType,
    val currency: CurrencyOption?,
    val status: PercentPartStatus = when {
        wholeValue.compareTo(BigDecimal.ZERO) == 0 -> PercentPartStatus.UNDEFINED
        partValue > wholeValue -> PercentPartStatus.PART_EXCEEDS_WHOLE
        else -> PercentPartStatus.NORMAL
    },
    val partMinorUnits: Long? = null,
    val wholeMinorUnits: Long? = null
) {
    val excessPercentage: BigDecimal?
        get() = if (status == PercentPartStatus.PART_EXCEEDS_WHOLE && percentage != null) {
            percentage.subtract(BigDecimal("100"))
        } else {
            null
        }
}
