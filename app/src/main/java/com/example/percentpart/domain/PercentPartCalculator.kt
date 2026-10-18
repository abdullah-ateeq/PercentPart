package com.example.percentpart.domain

import com.example.percentpart.model.CurrencyOption
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.model.PercentPartResult
import com.example.percentpart.model.PercentPartStatus
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

object PercentPartCalculator {
    private val HUNDRED = BigDecimal("100")
    val INTERNAL_MATH_CONTEXT = MathContext(34, RoundingMode.HALF_UP)

    fun calculate(
        partValue: BigDecimal,
        wholeValue: BigDecimal,
        inputType: PercentInputType = PercentInputType.NUMBER,
        currency: CurrencyOption? = null
    ): PercentPartResult {
        val partMinorUnits = if (inputType == PercentInputType.CURRENCY && currency != null) {
            MoneyParser.bigDecimalToMinorUnits(partValue, currency)
        } else {
            null
        }
        val wholeMinorUnits = if (inputType == PercentInputType.CURRENCY && currency != null) {
            MoneyParser.bigDecimalToMinorUnits(wholeValue, currency)
        } else {
            null
        }

        val authoritativePart = if (partMinorUnits != null && currency != null) {
            MoneyParser.minorUnitsToBigDecimal(partMinorUnits, currency)
        } else {
            partValue
        }
        val authoritativeWhole = if (wholeMinorUnits != null && currency != null) {
            MoneyParser.minorUnitsToBigDecimal(wholeMinorUnits, currency)
        } else {
            wholeValue
        }

        if (authoritativeWhole.compareTo(BigDecimal.ZERO) == 0) {
            return PercentPartResult(
                partValue = authoritativePart,
                wholeValue = authoritativeWhole,
                percentage = null,
                remainingPercentage = null,
                inputType = inputType,
                currency = currency,
                status = PercentPartStatus.UNDEFINED,
                partMinorUnits = partMinorUnits,
                wholeMinorUnits = wholeMinorUnits
            )
        }

        val rawPercentage = authoritativePart
            .divide(authoritativeWhole, INTERNAL_MATH_CONTEXT)
            .multiply(HUNDRED)
            .normalizeZeroAndTrailing()

        val rawRemaining = HUNDRED
            .subtract(rawPercentage)
            .normalizeZeroAndTrailing()

        val status = determineStatus(authoritativePart, authoritativeWhole)

        return PercentPartResult(
            partValue = authoritativePart,
            wholeValue = authoritativeWhole,
            percentage = rawPercentage,
            remainingPercentage = rawRemaining,
            inputType = inputType,
            currency = currency,
            status = status,
            partMinorUnits = partMinorUnits,
            wholeMinorUnits = wholeMinorUnits
        )
    }

    fun calculateFromMinorUnits(
        partMinorUnits: Long,
        wholeMinorUnits: Long,
        currency: CurrencyOption
    ): PercentPartResult {
        val partDecimal = MoneyParser.minorUnitsToBigDecimal(partMinorUnits, currency)
        val wholeDecimal = MoneyParser.minorUnitsToBigDecimal(wholeMinorUnits, currency)
        return calculate(
            partValue = partDecimal,
            wholeValue = wholeDecimal,
            inputType = PercentInputType.CURRENCY,
            currency = currency
        )
    }

    fun determineStatus(partValue: BigDecimal, wholeValue: BigDecimal): PercentPartStatus {
        return when {
            wholeValue.compareTo(BigDecimal.ZERO) == 0 -> PercentPartStatus.UNDEFINED
            partValue > wholeValue -> PercentPartStatus.PART_EXCEEDS_WHOLE
            else -> PercentPartStatus.NORMAL
        }
    }

    private fun BigDecimal.normalizeZeroAndTrailing(): BigDecimal {
        if (this.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        val stripped = this.stripTrailingZeros()
        // Avoid scientific notation for integers like 250 -> 2.5E+2 when scale < 0
        return if (stripped.scale() < 0) {
            stripped.setScale(0, RoundingMode.UNNECESSARY)
        } else {
            stripped
        }
    }
}
