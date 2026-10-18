package com.example.percentpart.domain

import com.example.percentpart.model.CurrencyOption
import com.example.percentpart.model.PercentInputType
import java.math.BigDecimal

sealed interface ValidationResult {
    data class Valid(
        val partValue: BigDecimal,
        val wholeValue: BigDecimal,
        val partMinorUnits: Long? = null,
        val wholeMinorUnits: Long? = null
    ) : ValidationResult

    data class ZeroWhole(
        val partValue: BigDecimal,
        val wholeValue: BigDecimal = BigDecimal.ZERO,
        val message: String,
        val partMinorUnits: Long? = null,
        val wholeMinorUnits: Long? = 0L
    ) : ValidationResult

    data class Invalid(
        val message: String,
        val isIncomplete: Boolean = false
    ) : ValidationResult
}

object PercentPartValidator {
    const val ERROR_NEGATIVE = "Values cannot be negative."
    const val ERROR_ZERO_WHOLE = "Whole value cannot be zero."
    const val ERROR_ZERO_OF_ZERO = "Percentage is undefined when the whole value is zero."
    const val ERROR_TOO_MANY_DECIMALS = "Value has too many decimal places."
    const val ERROR_TOO_LARGE = "Value is too large."
    const val ERROR_EMPTY = "Enter both part and whole values."
    const val ERROR_INVALID_NUMBER = "Enter a valid number."

    fun validate(
        partInput: String,
        wholeInput: String,
        inputType: PercentInputType = PercentInputType.NUMBER,
        currency: CurrencyOption? = null
    ): ValidationResult {
        val rawPart = partInput.trim()
        val rawWhole = wholeInput.trim()

        if (rawPart.isEmpty() || rawWhole.isEmpty()) {
            return ValidationResult.Invalid(ERROR_EMPTY, isIncomplete = true)
        }

        val cleanedPart = if (inputType == PercentInputType.CURRENCY) {
            MoneyParser.stripCurrencyDecorations(rawPart, currency)
        } else {
            NumberParser.normalizeInput(rawPart)
        }

        val cleanedWhole = if (inputType == PercentInputType.CURRENCY) {
            MoneyParser.stripCurrencyDecorations(rawWhole, currency)
        } else {
            NumberParser.normalizeInput(rawWhole)
        }

        if (cleanedPart == "." || cleanedWhole == "." || cleanedPart == "-" || cleanedWhole == "-") {
            if (cleanedPart.startsWith("-") || cleanedWhole.startsWith("-")) {
                return ValidationResult.Invalid(ERROR_NEGATIVE)
            }
            return ValidationResult.Invalid(ERROR_EMPTY, isIncomplete = true)
        }

        if (!NumberParser.isValidSyntax(cleanedPart) || !NumberParser.isValidSyntax(cleanedWhole)) {
            return ValidationResult.Invalid(ERROR_INVALID_NUMBER)
        }

        val partDecimal = NumberParser.parse(cleanedPart)
            ?: return ValidationResult.Invalid(ERROR_INVALID_NUMBER)
        val wholeDecimal = NumberParser.parse(cleanedWhole)
            ?: return ValidationResult.Invalid(ERROR_INVALID_NUMBER)

        if (partDecimal < BigDecimal.ZERO || wholeDecimal < BigDecimal.ZERO) {
            return ValidationResult.Invalid(ERROR_NEGATIVE)
        }

        val maxAllowedDecimals = if (inputType == PercentInputType.CURRENCY && currency != null) {
            currency.decimalPlaces
        } else {
            NumberParser.MAX_DECIMAL_PLACES
        }

        val partDecimals = NumberParser.countDecimalPlaces(cleanedPart)
        val wholeDecimals = NumberParser.countDecimalPlaces(cleanedWhole)
        if (partDecimals > maxAllowedDecimals || wholeDecimals > maxAllowedDecimals) {
            return ValidationResult.Invalid(ERROR_TOO_MANY_DECIMALS)
        }

        if (NumberParser.exceedsMaxValue(partDecimal) || NumberParser.exceedsMaxValue(wholeDecimal)) {
            return ValidationResult.Invalid(ERROR_TOO_LARGE)
        }

        var partMinorUnits: Long? = null
        var wholeMinorUnits: Long? = null

        if (inputType == PercentInputType.CURRENCY && currency != null) {
            partMinorUnits = MoneyParser.bigDecimalToMinorUnits(partDecimal, currency)
            wholeMinorUnits = MoneyParser.bigDecimalToMinorUnits(wholeDecimal, currency)
            if (partMinorUnits == null || wholeMinorUnits == null) {
                return ValidationResult.Invalid(ERROR_TOO_LARGE)
            }
        }

        if (wholeDecimal.compareTo(BigDecimal.ZERO) == 0) {
            val zeroMessage = if (partDecimal.compareTo(BigDecimal.ZERO) == 0) {
                ERROR_ZERO_OF_ZERO
            } else {
                ERROR_ZERO_WHOLE
            }
            return ValidationResult.ZeroWhole(
                partValue = if (partMinorUnits != null && currency != null) {
                    MoneyParser.minorUnitsToBigDecimal(partMinorUnits, currency)
                } else {
                    partDecimal
                },
                wholeValue = BigDecimal.ZERO,
                message = zeroMessage,
                partMinorUnits = partMinorUnits,
                wholeMinorUnits = 0L
            )
        }

        val resolvedPart = if (partMinorUnits != null && currency != null) {
            MoneyParser.minorUnitsToBigDecimal(partMinorUnits, currency)
        } else {
            partDecimal
        }
        val resolvedWhole = if (wholeMinorUnits != null && currency != null) {
            MoneyParser.minorUnitsToBigDecimal(wholeMinorUnits, currency)
        } else {
            wholeDecimal
        }

        return ValidationResult.Valid(
            partValue = resolvedPart,
            wholeValue = resolvedWhole,
            partMinorUnits = partMinorUnits,
            wholeMinorUnits = wholeMinorUnits
        )
    }

    fun isValidPartValue(partValue: BigDecimal): Boolean {
        return partValue >= BigDecimal.ZERO && !NumberParser.exceedsMaxValue(partValue)
    }

    fun isValidWholeValue(wholeValue: BigDecimal): Boolean {
        return wholeValue > BigDecimal.ZERO && !NumberParser.exceedsMaxValue(wholeValue)
    }
}
