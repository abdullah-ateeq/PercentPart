package com.example.percentpart.domain

import com.example.percentpart.model.CurrencyOption
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.model.PercentPartResult
import com.example.percentpart.model.PercentPartStatus
import java.math.BigDecimal
import java.math.RoundingMode

object PercentPartFormatter {

    fun formatNumber(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        val isNegative = value < BigDecimal.ZERO
        val stripped = value.abs().stripTrailingZeros()
        val normalized = if (stripped.scale() < 0) {
            stripped.setScale(0, RoundingMode.UNNECESSARY)
        } else {
            stripped
        }
        val plain = normalized.toPlainString()
        val parts = plain.split('.')
        val intFormatted = MoneyFormatter.formatIntegerWithCommas(parts[0])
        val sign = if (isNegative) "-" else ""
        return if (parts.size > 1 && parts[1].isNotEmpty()) {
            "$sign$intFormatted.${parts[1]}"
        } else {
            "$sign$intFormatted"
        }
    }

    fun formatMoney(minorUnits: Long, currency: CurrencyOption): String {
        return MoneyFormatter.format(minorUnits, currency)
    }

    fun formatPercentage(percentage: BigDecimal?): String {
        return PercentageFormatter.format(percentage)
    }

    fun formatValueForResult(
        value: BigDecimal,
        minorUnits: Long?,
        inputType: PercentInputType,
        currency: CurrencyOption?
    ): String {
        return if (inputType == PercentInputType.CURRENCY && currency != null) {
            if (minorUnits != null) {
                formatMoney(minorUnits, currency)
            } else {
                MoneyFormatter.formatBigDecimal(value, currency)
            }
        } else {
            formatNumber(value)
        }
    }

    fun formatRemainingLabel(result: PercentPartResult): String {
        return when (result.status) {
            PercentPartStatus.NORMAL -> {
                val rem = formatPercentage(result.remainingPercentage)
                "$rem remaining"
            }
            PercentPartStatus.PART_EXCEEDS_WHOLE -> {
                val excess = formatPercentage(result.excessPercentage)
                "Part exceeds whole by $excess."
            }
            PercentPartStatus.UNDEFINED -> {
                "Percentage unavailable"
            }
        }
    }

    fun formatCopyText(result: PercentPartResult): String {
        val partDisplay = formatValueForResult(
            value = result.partValue,
            minorUnits = result.partMinorUnits,
            inputType = result.inputType,
            currency = result.currency
        )
        val wholeDisplay = formatValueForResult(
            value = result.wholeValue,
            minorUnits = result.wholeMinorUnits,
            inputType = result.inputType,
            currency = result.currency
        )
        val percentageDisplay = formatPercentage(result.percentage)

        return buildString {
            appendLine("PercentPart Result")
            appendLine()
            appendLine("Part: $partDisplay")
            appendLine("Whole: $wholeDisplay")
            appendLine()
            append("Percentage: $percentageDisplay")
            when (result.status) {
                PercentPartStatus.NORMAL -> {
                    val remainingDisplay = formatPercentage(result.remainingPercentage)
                    appendLine()
                    append("Remaining: $remainingDisplay")
                }
                PercentPartStatus.PART_EXCEEDS_WHOLE -> {
                    val excessDisplay = formatPercentage(result.excessPercentage)
                    appendLine()
                    append("Status: Part exceeds whole by $excessDisplay.")
                }
                PercentPartStatus.UNDEFINED -> {
                    appendLine()
                    append("Status: Whole value cannot be zero.")
                }
            }
        }
    }

    fun formatShareText(result: PercentPartResult): String {
        val partDisplay = formatValueForResult(
            value = result.partValue,
            minorUnits = result.partMinorUnits,
            inputType = result.inputType,
            currency = result.currency
        )
        val wholeDisplay = formatValueForResult(
            value = result.wholeValue,
            minorUnits = result.wholeMinorUnits,
            inputType = result.inputType,
            currency = result.currency
        )
        val percentageDisplay = formatPercentage(result.percentage)

        return buildString {
            appendLine("PercentPart")
            appendLine()
            appendLine("Part: $partDisplay")
            appendLine("Whole: $wholeDisplay")
            appendLine()
            when (result.status) {
                PercentPartStatus.NORMAL -> {
                    val remainingDisplay = formatPercentage(result.remainingPercentage)
                    appendLine("$percentageDisplay of the whole")
                    append("$remainingDisplay remaining")
                }
                PercentPartStatus.PART_EXCEEDS_WHOLE -> {
                    val excessDisplay = formatPercentage(result.excessPercentage)
                    appendLine("$percentageDisplay of the whole")
                    append("Part exceeds whole by $excessDisplay.")
                }
                PercentPartStatus.UNDEFINED -> {
                    append("Percentage unavailable (Whole value cannot be zero)")
                }
            }
        }
    }

    fun formatHistoryPartValue(item: com.example.percentpart.model.PercentPartHistory): String {
        val bd = NumberParser.parse(item.partValue) ?: return item.partValue
        return if (item.inputType == PercentInputType.CURRENCY) {
            val currency = CurrencyRepository.getByCode(
                code = item.currencyCode,
                customSymbol = item.currencySymbol ?: "¤",
                customDecimalPlaces = item.currencyDecimalPlaces ?: 2
            )
            MoneyFormatter.formatBigDecimal(bd, currency)
        } else {
            formatNumber(bd)
        }
    }

    fun formatHistoryWholeValue(item: com.example.percentpart.model.PercentPartHistory): String {
        val bd = NumberParser.parse(item.wholeValue) ?: return item.wholeValue
        return if (item.inputType == PercentInputType.CURRENCY) {
            val currency = CurrencyRepository.getByCode(
                code = item.currencyCode,
                customSymbol = item.currencySymbol ?: "¤",
                customDecimalPlaces = item.currencyDecimalPlaces ?: 2
            )
            MoneyFormatter.formatBigDecimal(bd, currency)
        } else {
            formatNumber(bd)
        }
    }

    fun formatHistoryPercentage(item: com.example.percentpart.model.PercentPartHistory): String {
        val bd = NumberParser.parse(item.percentage) ?: return "${item.percentage}%"
        return formatPercentage(bd)
    }

    fun formatHistoryRemaining(item: com.example.percentpart.model.PercentPartHistory): String? {
        return when (item.status) {
            PercentPartStatus.NORMAL -> {
                val remBd = item.remainingPercentage?.let { NumberParser.parse(it) } ?: return null
                "${formatPercentage(remBd)} remaining"
            }
            PercentPartStatus.PART_EXCEEDS_WHOLE -> {
                val pctBd = NumberParser.parse(item.percentage) ?: return "Part exceeds whole"
                val excess = pctBd.subtract(BigDecimal("100"))
                "Part exceeds whole by ${formatPercentage(excess)}."
            }
            PercentPartStatus.UNDEFINED -> null
        }
    }
}
