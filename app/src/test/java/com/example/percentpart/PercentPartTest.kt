package com.example.percentpart

import com.example.percentpart.data.InMemoryKeyValueStore
import com.example.percentpart.domain.CurrencyRepository
import com.example.percentpart.domain.MoneyFormatter
import com.example.percentpart.domain.MoneyParser
import com.example.percentpart.domain.NumberParser
import com.example.percentpart.domain.PercentPartCalculator
import com.example.percentpart.domain.PercentPartFormatter
import com.example.percentpart.domain.PercentPartValidator
import com.example.percentpart.domain.PercentageFormatter
import com.example.percentpart.domain.ValidationResult
import com.example.percentpart.model.AppThemeMode
import com.example.percentpart.model.CalculationState
import com.example.percentpart.model.PercentInputType
import com.example.percentpart.model.PercentPartStatus
import com.example.percentpart.repository.PercentPartHistoryRepository
import com.example.percentpart.repository.SettingsRepository
import com.example.percentpart.ui.PercentPartViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PercentPartTest {

    // 1. 25 of 100
    @Test
    fun test01_25Of100_produces25Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("25"),
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("25"), result.percentage)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals(PercentPartStatus.NORMAL, result.status)
    }

    // 2. 50 of 100
    @Test
    fun test02_50Of100_produces50Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("50"),
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("50"), result.percentage)
        assertEquals("50%", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 3. 75 of 100
    @Test
    fun test03_75Of100_produces75Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("75"),
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("75"), result.percentage)
        assertEquals("75%", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 4. 100 of 100
    @Test
    fun test04_100Of100_produces100Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("100"),
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("100"), result.percentage)
        assertEquals(BigDecimal.ZERO, result.remainingPercentage)
        assertEquals("100%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals(PercentPartStatus.NORMAL, result.status)
    }

    // 5. 0 of 100
    @Test
    fun test05_0Of100_produces0Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal.ZERO,
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal.ZERO, result.percentage)
        assertEquals(BigDecimal("100"), result.remainingPercentage)
        assertEquals("0%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals(PercentPartStatus.NORMAL, result.status)
    }

    // 6. 150 of 100
    @Test
    fun test06_150Of100_produces150Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("150"),
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("150"), result.percentage)
        assertEquals("150%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals(PercentPartStatus.PART_EXCEEDS_WHOLE, result.status)
    }

    // 7. 25 of 200
    @Test
    fun test07_25Of200_produces12Point5Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("25"),
            wholeValue = BigDecimal("200"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("12.5"), result.percentage)
        assertEquals("12.5%", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 8. 30 of 120
    @Test
    fun test08_30Of120_produces25Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("30"),
            wholeValue = BigDecimal("120"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("25"), result.percentage)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 9. 1 of 4
    @Test
    fun test09_1Of4_produces25Percent() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("1"),
            wholeValue = BigDecimal("4"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertEquals(BigDecimal("25"), result.percentage)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 10. Decimal values (12.5 of 50, 2.5 of 10, 33.33 of 100)
    @Test
    fun test10_decimalValues_calculateAccurately() {
        val res1 = PercentPartCalculator.calculate(BigDecimal("12.5"), BigDecimal("50"))
        assertEquals("25%", PercentPartFormatter.formatPercentage(res1.percentage))

        val res2 = PercentPartCalculator.calculate(BigDecimal("2.5"), BigDecimal("10"))
        assertEquals("25%", PercentPartFormatter.formatPercentage(res2.percentage))

        val res3 = PercentPartCalculator.calculate(BigDecimal("33.33"), BigDecimal("100"))
        assertEquals("33.33%", PercentPartFormatter.formatPercentage(res3.percentage))
    }

    // 11. Decimal percentage (1 of 3 = 33.33%, 2 of 3 = 66.67%)
    @Test
    fun test11_decimalPercentage_roundsHalfUpForDisplay() {
        val oneThird = PercentPartCalculator.calculate(BigDecimal("1"), BigDecimal("3"))
        assertTrue(oneThird.percentage!!.scale() > 10) // High internal precision preserved
        assertEquals("33.33%", PercentPartFormatter.formatPercentage(oneThird.percentage))

        val twoThirds = PercentPartCalculator.calculate(BigDecimal("2"), BigDecimal("3"))
        assertEquals("66.67%", PercentPartFormatter.formatPercentage(twoThirds.percentage))
    }

    // 12. Zero part
    @Test
    fun test12_zeroPart_isValidAndProducesZeroPercent() {
        val validation = PercentPartValidator.validate("0", "100", PercentInputType.NUMBER, null)
        assertTrue(validation is ValidationResult.Valid)
        val result = PercentPartCalculator.calculate(BigDecimal.ZERO, BigDecimal("100"))
        assertEquals("0%", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 13. Zero whole
    @Test
    fun test13_zeroWhole_returnsUndefinedAndError() {
        val validation = PercentPartValidator.validate("25", "0", PercentInputType.NUMBER, null)
        assertTrue(validation is ValidationResult.ZeroWhole)
        assertEquals("Whole value cannot be zero.", (validation as ValidationResult.ZeroWhole).message)

        val result = PercentPartCalculator.calculate(BigDecimal("25"), BigDecimal.ZERO)
        assertNull(result.percentage)
        assertNull(result.remainingPercentage)
        assertEquals(PercentPartStatus.UNDEFINED, result.status)
        assertEquals("—", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 14. Zero of zero
    @Test
    fun test14_zeroOfZero_returnsUndefinedWithoutCrash() {
        val validation = PercentPartValidator.validate("0", "0", PercentInputType.NUMBER, null)
        assertTrue(validation is ValidationResult.ZeroWhole)
        assertEquals(
            "Percentage is undefined when the whole value is zero.",
            (validation as ValidationResult.ZeroWhole).message
        )

        val result = PercentPartCalculator.calculate(BigDecimal.ZERO, BigDecimal.ZERO)
        assertNull(result.percentage)
        assertEquals(PercentPartStatus.UNDEFINED, result.status)
        assertEquals("—", PercentPartFormatter.formatPercentage(result.percentage))
    }

    // 15. Negative part
    @Test
    fun test15_negativePart_isRejected() {
        val validation = PercentPartValidator.validate("-5", "100", PercentInputType.NUMBER, null)
        assertTrue(validation is ValidationResult.Invalid)
        assertEquals("Values cannot be negative.", (validation as ValidationResult.Invalid).message)
    }

    // 16. Negative whole
    @Test
    fun test16_negativeWhole_isRejected() {
        val validation = PercentPartValidator.validate("25", "-100", PercentInputType.NUMBER, null)
        assertTrue(validation is ValidationResult.Invalid)
        assertEquals("Values cannot be negative.", (validation as ValidationResult.Invalid).message)
    }

    // 17. Part greater than whole
    @Test
    fun test17_partGreaterThanWhole_isAllowedWithPartExceedsWholeStatus() {
        val result = PercentPartCalculator.calculate(BigDecimal("125"), BigDecimal("100"))
        assertEquals(BigDecimal("125"), result.percentage)
        assertEquals(PercentPartStatus.PART_EXCEEDS_WHOLE, result.status)
        assertEquals("125%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("Part exceeds whole by 25%.", PercentPartFormatter.formatRemainingLabel(result))
    }

    // 18. Core percentage calculation formula before display rounding
    @Test
    fun test18_percentageCalculation_usesExactFormulaBeforeDisplayRounding() {
        val part = BigDecimal("7")
        val whole = BigDecimal("16")
        // 7 / 16 * 100 = 43.75 exactly
        val result = PercentPartCalculator.calculate(part, whole)
        assertEquals(BigDecimal("43.75"), result.percentage)

        // 1 / 6 * 100 = 16.66666666666666666666666666666667 (34 digits precision)
        val repeating = PercentPartCalculator.calculate(BigDecimal("1"), BigDecimal("6"))
        assertTrue(repeating.percentage!!.toPlainString().startsWith("16.6666666666"))
        assertEquals("16.67%", PercentPartFormatter.formatPercentage(repeating.percentage))
    }

    // 19. Remaining percentage
    @Test
    fun test19_remainingPercentage_calculatedCorrectly() {
        val normal = PercentPartCalculator.calculate(BigDecimal("25"), BigDecimal("100"))
        assertEquals(BigDecimal("75"), normal.remainingPercentage)
        assertEquals("75% remaining", PercentPartFormatter.formatRemainingLabel(normal))

        val exceeds = PercentPartCalculator.calculate(BigDecimal("125"), BigDecimal("100"))
        assertEquals(BigDecimal("-25"), exceeds.remainingPercentage)
        assertEquals(BigDecimal("25"), exceeds.excessPercentage)
        assertEquals("Part exceeds whole by 25%.", PercentPartFormatter.formatRemainingLabel(exceeds))
    }

    // 20. Status detection
    @Test
    fun test20_statusDetection_coversAllThreeStates() {
        assertEquals(
            PercentPartStatus.NORMAL,
            PercentPartCalculator.determineStatus(BigDecimal("25"), BigDecimal("100"))
        )
        assertEquals(
            PercentPartStatus.NORMAL,
            PercentPartCalculator.determineStatus(BigDecimal("100"), BigDecimal("100"))
        )
        assertEquals(
            PercentPartStatus.PART_EXCEEDS_WHOLE,
            PercentPartCalculator.determineStatus(BigDecimal("100.01"), BigDecimal("100"))
        )
        assertEquals(
            PercentPartStatus.UNDEFINED,
            PercentPartCalculator.determineStatus(BigDecimal("25"), BigDecimal.ZERO)
        )
    }

    // 21. BigDecimal precision (0.1 of 0.3)
    @Test
    fun test21_bigDecimalPrecision_0Point1Of0Point3() {
        val result = PercentPartCalculator.calculate(BigDecimal("0.1"), BigDecimal("0.3"))
        assertEquals("33.33%", PercentPartFormatter.formatPercentage(result.percentage))
        // 0.1 + 0.2 out of 0.3 = 100% exactly without IEEE 754 float drift
        val exactSum = PercentPartCalculator.calculate(
            BigDecimal("0.1").add(BigDecimal("0.2")),
            BigDecimal("0.3")
        )
        assertEquals(BigDecimal("100"), exactSum.percentage)
        assertEquals("100%", PercentPartFormatter.formatPercentage(exactSum.percentage))
    }

    // 22. Large values
    @Test
    fun test22_largeValues_supportedAndOverflowRejectedSafely() {
        val largePart = BigDecimal("250000000000000000000000000000")
        val largeWhole = BigDecimal("1000000000000000000000000000000")
        val result = PercentPartCalculator.calculate(largePart, largeWhole)
        assertEquals(BigDecimal("25"), result.percentage)

        // Exceeding 10^30 - 1 returns "Value is too large."
        val tooLargeValidation = PercentPartValidator.validate(
            partInput = "10000000000000000000000000000000",
            wholeInput = "100",
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertTrue(tooLargeValidation is ValidationResult.Invalid)
        assertEquals("Value is too large.", (tooLargeValidation as ValidationResult.Invalid).message)
    }

    // 23. Invalid numeric input
    @Test
    fun test23_invalidNumericInput_isRejected() {
        val invalid1 = PercentPartValidator.validate("abc", "100", PercentInputType.NUMBER, null)
        assertTrue(invalid1 is ValidationResult.Invalid)
        assertEquals("Enter a valid number.", (invalid1 as ValidationResult.Invalid).message)

        val invalid2 = PercentPartValidator.validate("25..5", "100", PercentInputType.NUMBER, null)
        assertTrue(invalid2 is ValidationResult.Invalid)
    }

    // 24. Empty input
    @Test
    fun test24_emptyInput_isMarkedIncomplete() {
        val emptyPart = PercentPartValidator.validate("", "100", PercentInputType.NUMBER, null)
        assertTrue(emptyPart is ValidationResult.Invalid)
        assertTrue((emptyPart as ValidationResult.Invalid).isIncomplete)

        val emptyWhole = PercentPartValidator.validate("25", "   ", PercentInputType.NUMBER, null)
        assertTrue(emptyWhole is ValidationResult.Invalid)
        assertTrue((emptyWhole as ValidationResult.Invalid).isIncomplete)
    }

    // 25. Excess decimal places
    @Test
    fun test25_excessDecimalPlaces_supports12AndRejects13() {
        val valid12 = PercentPartValidator.validate(
            partInput = "1.123456789012",
            wholeInput = "100",
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertTrue(valid12 is ValidationResult.Valid)

        val invalid13 = PercentPartValidator.validate(
            partInput = "1.1234567890123",
            wholeInput = "100",
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        assertTrue(invalid13 is ValidationResult.Invalid)
        assertEquals("Value has too many decimal places.", (invalid13 as ValidationResult.Invalid).message)
    }

    // 26. Currency mode minor units representation
    @Test
    fun test26_currencyMode_storesMinorUnitsAsLong() {
        val partMinor = MoneyParser.parseToMinorUnits("$25.00", CurrencyRepository.USD)
        val wholeMinor = MoneyParser.parseToMinorUnits("$100.00", CurrencyRepository.USD)
        assertEquals(2500L, partMinor)
        assertEquals(10000L, wholeMinor)

        val result = PercentPartCalculator.calculateFromMinorUnits(
            partMinorUnits = partMinor!!,
            wholeMinorUnits = wholeMinor!!,
            currency = CurrencyRepository.USD
        )
        assertEquals(2500L, result.partMinorUnits)
        assertEquals(10000L, result.wholeMinorUnits)
        assertEquals(BigDecimal("25"), result.percentage)
    }

    // 27. USD
    @Test
    fun test27_currencyUSD() {
        val result = PercentPartCalculator.calculateFromMinorUnits(2500L, 10000L, CurrencyRepository.USD)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("$25.00", PercentPartFormatter.formatMoney(result.partMinorUnits!!, CurrencyRepository.USD))
        assertEquals("$100.00", PercentPartFormatter.formatMoney(result.wholeMinorUnits!!, CurrencyRepository.USD))
    }

    // 28. EUR
    @Test
    fun test28_currencyEUR() {
        val result = PercentPartCalculator.calculateFromMinorUnits(2500L, 10000L, CurrencyRepository.EUR)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("€25.00", PercentPartFormatter.formatMoney(result.partMinorUnits!!, CurrencyRepository.EUR))
    }

    // 29. GBP
    @Test
    fun test29_currencyGBP() {
        val result = PercentPartCalculator.calculateFromMinorUnits(2500L, 10000L, CurrencyRepository.GBP)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("£25.00", PercentPartFormatter.formatMoney(result.partMinorUnits!!, CurrencyRepository.GBP))
    }

    // 30. PKR
    @Test
    fun test30_currencyPKR() {
        val partMinor = MoneyParser.parseToMinorUnits("Rs. 250.00", CurrencyRepository.PKR)
        val wholeMinor = MoneyParser.parseToMinorUnits("Rs. 1,000.00", CurrencyRepository.PKR)
        assertEquals(25000L, partMinor)
        assertEquals(100000L, wholeMinor)
        val result = PercentPartCalculator.calculateFromMinorUnits(partMinor!!, wholeMinor!!, CurrencyRepository.PKR)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("Rs. 250.00", PercentPartFormatter.formatMoney(partMinor, CurrencyRepository.PKR))
        assertEquals("Rs. 1,000.00", PercentPartFormatter.formatMoney(wholeMinor, CurrencyRepository.PKR))
    }

    // 31. AED
    @Test
    fun test31_currencyAED() {
        val result = PercentPartCalculator.calculateFromMinorUnits(5000L, 20000L, CurrencyRepository.AED)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("AED 50.00", PercentPartFormatter.formatMoney(5000L, CurrencyRepository.AED))
    }

    // 32. SAR
    @Test
    fun test32_currencySAR() {
        val result = PercentPartCalculator.calculateFromMinorUnits(5000L, 20000L, CurrencyRepository.SAR)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("SAR 50.00", PercentPartFormatter.formatMoney(5000L, CurrencyRepository.SAR))
    }

    // 33. CAD
    @Test
    fun test33_currencyCAD() {
        val result = PercentPartCalculator.calculateFromMinorUnits(2500L, 10000L, CurrencyRepository.CAD)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("CA$25.00", PercentPartFormatter.formatMoney(2500L, CurrencyRepository.CAD))
    }

    // 34. AUD
    @Test
    fun test34_currencyAUD() {
        val result = PercentPartCalculator.calculateFromMinorUnits(2500L, 10000L, CurrencyRepository.AUD)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("A$25.00", PercentPartFormatter.formatMoney(2500L, CurrencyRepository.AUD))
    }

    // 35. INR
    @Test
    fun test35_currencyINR() {
        val result = PercentPartCalculator.calculateFromMinorUnits(25000L, 100000L, CurrencyRepository.INR)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("₹250.00", PercentPartFormatter.formatMoney(25000L, CurrencyRepository.INR))
    }

    // 36. JPY
    @Test
    fun test36_currencyJPY() {
        val partMinor = MoneyParser.parseToMinorUnits("¥25", CurrencyRepository.JPY)
        val wholeMinor = MoneyParser.parseToMinorUnits("¥100", CurrencyRepository.JPY)
        assertEquals(25L, partMinor)
        assertEquals(100L, wholeMinor)
        val result = PercentPartCalculator.calculateFromMinorUnits(partMinor!!, wholeMinor!!, CurrencyRepository.JPY)
        assertEquals("25%", PercentPartFormatter.formatPercentage(result.percentage))
        assertEquals("¥25", PercentPartFormatter.formatMoney(partMinor, CurrencyRepository.JPY))
        assertEquals("¥100", PercentPartFormatter.formatMoney(wholeMinor, CurrencyRepository.JPY))
    }

    // 37. Zero-decimal currency validation
    @Test
    fun test37_zeroDecimalCurrency_rejectsFractionalInput() {
        val validation = PercentPartValidator.validate(
            partInput = "25.5",
            wholeInput = "100",
            inputType = PercentInputType.CURRENCY,
            currency = CurrencyRepository.JPY
        )
        assertTrue(validation is ValidationResult.Invalid)
        assertEquals("Value has too many decimal places.", (validation as ValidationResult.Invalid).message)
    }

    // 38. Two-decimal currency validation
    @Test
    fun test38_twoDecimalCurrency_allowsTwoAndRejectsThreeDecimals() {
        val valid = PercentPartValidator.validate(
            partInput = "25.50",
            wholeInput = "100.00",
            inputType = PercentInputType.CURRENCY,
            currency = CurrencyRepository.CNY
        )
        assertTrue(valid is ValidationResult.Valid)

        val invalid = PercentPartValidator.validate(
            partInput = "25.505",
            wholeInput = "100.00",
            inputType = PercentInputType.CURRENCY,
            currency = CurrencyRepository.CNY
        )
        assertTrue(invalid is ValidationResult.Invalid)
        assertEquals("Value has too many decimal places.", (invalid as ValidationResult.Invalid).message)
    }

    // 39. Custom currency (0-4 decimal places)
    @Test
    fun test39_customCurrency_supports0To4DecimalPlaces() {
        val custom3 = CurrencyRepository.createCustomCurrency(symbol = "KWD", decimalPlaces = 3)
        assertEquals(3, custom3.decimalPlaces)
        val valid3 = PercentPartValidator.validate("25.125", "100.500", PercentInputType.CURRENCY, custom3)
        assertTrue(valid3 is ValidationResult.Valid)
        val invalid4On3 = PercentPartValidator.validate("25.1254", "100.500", PercentInputType.CURRENCY, custom3)
        assertTrue(invalid4On3 is ValidationResult.Invalid)

        val custom4 = CurrencyRepository.createCustomCurrency(symbol = "TOKEN", decimalPlaces = 4)
        assertEquals(4, custom4.decimalPlaces)
        val valid4 = PercentPartValidator.validate("1.2345", "10.0000", PercentInputType.CURRENCY, custom4)
        assertTrue(valid4 is ValidationResult.Valid)
    }

    // 40. Money formatting
    @Test
    fun test40_moneyFormatting_formatsSymbolsCommasAndDecimals() {
        assertEquals("$25.00", MoneyFormatter.format(2500L, CurrencyRepository.USD))
        assertEquals("$1,250.50", MoneyFormatter.format(125050L, CurrencyRepository.USD))
        assertEquals("¥1,000", MoneyFormatter.format(1000L, CurrencyRepository.JPY))
        assertEquals("Rs. 1,000.00", MoneyFormatter.format(100000L, CurrencyRepository.PKR))
    }

    // 41. Number formatting
    @Test
    fun test41_numberFormatting_stripsUnnecessaryTrailingZerosAndAddsCommas() {
        assertEquals("0", PercentPartFormatter.formatNumber(BigDecimal("0.000")))
        assertEquals("25", PercentPartFormatter.formatNumber(BigDecimal("25.00")))
        assertEquals("1,000", PercentPartFormatter.formatNumber(BigDecimal("1000")))
        assertEquals("1,234.5678", PercentPartFormatter.formatNumber(BigDecimal("1234.567800")))
    }

    // 42. Percentage formatting
    @Test
    fun test42_percentageFormatting_stripsTrailingZerosUpToTwoDecimals() {
        assertEquals("25%", PercentPartFormatter.formatPercentage(BigDecimal("25.000")))
        assertEquals("12.5%", PercentPartFormatter.formatPercentage(BigDecimal("12.50")))
        assertEquals("33.33%", PercentPartFormatter.formatPercentage(BigDecimal("33.3333333")))
        assertEquals("66.67%", PercentPartFormatter.formatPercentage(BigDecimal("66.6666667")))
        assertEquals("125%", PercentPartFormatter.formatPercentage(BigDecimal("125")))
        assertEquals("—", PercentPartFormatter.formatPercentage(null))
    }

    // 43. Percentage rounding (HALF_UP)
    @Test
    fun test43_percentageRounding_usesHalfUp() {
        assertEquals("12.35%", PercentPartFormatter.formatPercentage(BigDecimal("12.345")))
        assertEquals("12.34%", PercentPartFormatter.formatPercentage(BigDecimal("12.344")))
        assertEquals("0.01%", PercentPartFormatter.formatPercentage(BigDecimal("0.005")))
        assertEquals("0%", PercentPartFormatter.formatPercentage(BigDecimal("0.004")))
    }

    // 44. Swap values
    @Test
    fun test44_swapValues_exchangesOnlyPartAndWholeAndRecalculates() {
        val vm = PercentPartViewModel()
        assertEquals("25", vm.uiState.value.partValueInput)
        assertEquals("100", vm.uiState.value.wholeValueInput)
        assertEquals("25%", PercentPartFormatter.formatPercentage(vm.uiState.value.result?.percentage))

        vm.onInputTypeChanged(PercentInputType.CURRENCY)
        vm.onCurrencyChanged(CurrencyRepository.EUR)
        vm.saveCurrentResultToHistory()
        val historyBeforeSwap = vm.uiState.value.history

        vm.swapValues()

        assertEquals("100", vm.uiState.value.partValueInput)
        assertEquals("25", vm.uiState.value.wholeValueInput)
        assertEquals("400%", PercentPartFormatter.formatPercentage(vm.uiState.value.result?.percentage))
        assertEquals(PercentPartStatus.PART_EXCEEDS_WHOLE, vm.uiState.value.result?.status)
        // Input type, currency, and history must not change on swap
        assertEquals(PercentInputType.CURRENCY, vm.uiState.value.inputType)
        assertEquals(CurrencyRepository.EUR, vm.uiState.value.currency)
        assertEquals(historyBeforeSwap, vm.uiState.value.history)
    }

    // 45. History save
    @Test
    fun test45_historySave_savesOnlyOnExplicitSaveAction() {
        val vm = PercentPartViewModel()
        vm.onPartValueChanged("50")
        vm.onWholeValueChanged("200")
        // Automatic recalculation must not save to history
        assertTrue(vm.uiState.value.history.isEmpty())

        val saved = vm.saveCurrentResultToHistory()
        assertTrue(saved)
        assertEquals(1, vm.uiState.value.history.size)
        assertEquals("50", vm.uiState.value.history.first().partValue)
        assertEquals("200", vm.uiState.value.history.first().wholeValue)
        assertEquals("25", vm.uiState.value.history.first().percentage)
    }

    // 46. History load
    @Test
    fun test46_historyLoad_persistsAcrossRepositoryInstances() {
        val store = InMemoryKeyValueStore()
        val repo1 = PercentPartHistoryRepository(store)
        val vm1 = PercentPartViewModel(historyRepository = repo1)
        vm1.onCommonExampleSelected("30", "120")
        vm1.saveCurrentResultToHistory()

        val repo2 = PercentPartHistoryRepository(store)
        val loaded = repo2.loadHistory()
        assertEquals(1, loaded.size)
        assertEquals("30", loaded[0].partValue)
        assertEquals("120", loaded[0].wholeValue)
        assertEquals("25", loaded[0].percentage)
    }

    // 47. History limit (max 10, 11th removes oldest)
    @Test
    fun test47_historyLimit_enforcesMax10AndEvictsOldest() {
        var fakeTime = 1000L
        val vm = PercentPartViewModel(clockProvider = {
            fakeTime += 1000L
            fakeTime
        })

        for (i in 1..11) {
            vm.onPartValueChanged(i.toString())
            vm.onWholeValueChanged("100")
            assertTrue(vm.saveCurrentResultToHistory())
        }

        val history = vm.uiState.value.history
        assertEquals(10, history.size)
        // Newest (11) is first; oldest (1) was removed so last is 2
        assertEquals("11", history.first().partValue)
        assertEquals("2", history.last().partValue)
        assertFalse(history.any { it.partValue == "1" })
    }

    // 48. History deletion
    @Test
    fun test48_historyDeletion_removesSpecificEntry() {
        var fakeTime = 1000L
        val vm = PercentPartViewModel(clockProvider = {
            fakeTime += 1000L
            fakeTime
        })
        vm.onCommonExampleSelected("25", "100")
        vm.saveCurrentResultToHistory()
        vm.onCommonExampleSelected("75", "100")
        vm.saveCurrentResultToHistory()

        assertEquals(2, vm.uiState.value.history.size)
        val firstId = vm.uiState.value.history.first().id
        vm.deleteHistoryItem(firstId)

        assertEquals(1, vm.uiState.value.history.size)
        assertEquals("25", vm.uiState.value.history.first().partValue)
    }

    // 49. History clear
    @Test
    fun test49_historyClear_removesAllEntries() {
        var fakeTime = 1000L
        val vm = PercentPartViewModel(clockProvider = {
            fakeTime += 1000L
            fakeTime
        })
        vm.onCommonExampleSelected("25", "100")
        vm.saveCurrentResultToHistory()
        vm.onCommonExampleSelected("50", "200")
        vm.saveCurrentResultToHistory()
        assertEquals(2, vm.uiState.value.history.size)

        vm.clearHistory()
        assertTrue(vm.uiState.value.history.isEmpty())
    }

    // 50. History restore
    @Test
    fun test50_historyRestore_restoresPartWholeInputTypeCurrencyAndRecalculates() {
        var fakeTime = 1000L
        val vm = PercentPartViewModel(clockProvider = {
            fakeTime += 1000L
            fakeTime
        })
        vm.onInputTypeChanged(PercentInputType.CURRENCY)
        vm.onCurrencyChanged(CurrencyRepository.GBP)
        vm.onPartValueChanged("30")
        vm.onWholeValueChanged("120")
        vm.saveCurrentResultToHistory()
        val savedItem = vm.uiState.value.history.first()

        // Change state to something else
        vm.reset()
        assertEquals("25", vm.uiState.value.partValueInput)
        assertEquals(PercentInputType.NUMBER, vm.uiState.value.inputType)

        // Restore history item
        vm.restoreHistoryItem(savedItem)
        assertEquals("30", vm.uiState.value.partValueInput)
        assertEquals("120", vm.uiState.value.wholeValueInput)
        assertEquals(PercentInputType.CURRENCY, vm.uiState.value.inputType)
        assertEquals(CurrencyRepository.GBP, vm.uiState.value.currency)
        assertEquals("25%", PercentPartFormatter.formatPercentage(vm.uiState.value.result?.percentage))
    }

    // 51. Settings persistence
    @Test
    fun test51_settingsPersistence_persistsThemeAndDefaultInputTypeAcrossSessions() {
        val store = InMemoryKeyValueStore()
        val settingsRepo1 = SettingsRepository(store)
        val vm1 = PercentPartViewModel(settingsRepository = settingsRepo1)

        vm1.updateThemeMode(AppThemeMode.DARK)
        vm1.updateDefaultInputType(PercentInputType.CURRENCY)
        vm1.updateDefaultCurrency("PKR")
        vm1.updateDynamicColor(false)
        vm1.updateHapticFeedback(false)
        vm1.updateAnimationsEnabled(false)
        vm1.updateConfirmHistoryDelete(false)

        // Reopen with new repository & ViewModel backed by same store
        val settingsRepo2 = SettingsRepository(store)
        val vm2 = PercentPartViewModel(settingsRepository = settingsRepo2)

        assertEquals(AppThemeMode.DARK, vm2.uiState.value.settings.themeMode)
        assertEquals(PercentInputType.CURRENCY, vm2.uiState.value.settings.defaultInputType)
        assertEquals("PKR", vm2.uiState.value.settings.defaultCurrencyCode)
        assertFalse(vm2.uiState.value.settings.dynamicColor)
        assertFalse(vm2.uiState.value.settings.hapticFeedback)
        assertFalse(vm2.uiState.value.settings.animationsEnabled)
        assertFalse(vm2.uiState.value.settings.confirmHistoryDelete)
    }

    // 52. Last-calculation persistence
    @Test
    fun test52_lastCalculationPersistence_respectsPreserveSetting() {
        val store = InMemoryKeyValueStore()
        val settingsRepo1 = SettingsRepository(store)
        val vm1 = PercentPartViewModel(settingsRepository = settingsRepo1)

        vm1.onInputTypeChanged(PercentInputType.CURRENCY)
        vm1.onCurrencyChanged(CurrencyRepository.CAD)
        vm1.onPartValueChanged("75")
        vm1.onWholeValueChanged("300")

        // When preserveLastCalculation is true (default), reopening restores 75, 300, CURRENCY, CAD
        val vm2 = PercentPartViewModel(settingsRepository = SettingsRepository(store))
        assertEquals("75", vm2.uiState.value.partValueInput)
        assertEquals("300", vm2.uiState.value.wholeValueInput)
        assertEquals(PercentInputType.CURRENCY, vm2.uiState.value.inputType)
        assertEquals("CAD", vm2.uiState.value.currency?.code)

        // Disable preserveLastCalculation -> reopening uses 25, 100, defaultInputType (NUMBER)
        vm2.updatePreserveLastCalculation(false)
        val vm3 = PercentPartViewModel(settingsRepository = SettingsRepository(store))
        assertEquals("25", vm3.uiState.value.partValueInput)
        assertEquals("100", vm3.uiState.value.wholeValueInput)
        assertEquals(PercentInputType.NUMBER, vm3.uiState.value.inputType)
    }

    // 53. Reset
    @Test
    fun test53_reset_restoresDefaultPartWholeAndInputTypeWhileKeepingHistory() {
        val vm = PercentPartViewModel()
        vm.onInputTypeChanged(PercentInputType.CURRENCY)
        vm.onPartValueChanged("80")
        vm.onWholeValueChanged("160")
        vm.saveCurrentResultToHistory()
        assertEquals(1, vm.uiState.value.history.size)

        vm.reset()

        assertEquals("25", vm.uiState.value.partValueInput)
        assertEquals("100", vm.uiState.value.wholeValueInput)
        assertEquals(PercentInputType.NUMBER, vm.uiState.value.inputType)
        assertEquals(BigDecimal("25"), vm.uiState.value.result?.percentage)
        assertEquals(1, vm.uiState.value.history.size)
    }

    // 54. Copy formatting
    @Test
    fun test54_copyFormatting_matchesNumberAndCurrencySpecifications() {
        val numberResult = PercentPartCalculator.calculate(
            partValue = BigDecimal("25"),
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        val expectedNumberCopy = """
            PercentPart Result

            Part: 25
            Whole: 100

            Percentage: 25%
            Remaining: 75%
        """.trimIndent()
        assertEquals(expectedNumberCopy, PercentPartFormatter.formatCopyText(numberResult))

        val currencyResult = PercentPartCalculator.calculateFromMinorUnits(
            partMinorUnits = 2500L,
            wholeMinorUnits = 10000L,
            currency = CurrencyRepository.USD
        )
        val expectedCurrencyCopy = """
            PercentPart Result

            Part: $25.00
            Whole: $100.00

            Percentage: 25%
            Remaining: 75%
        """.trimIndent()
        assertEquals(expectedCurrencyCopy, PercentPartFormatter.formatCopyText(currencyResult))
    }

    // 55. Share formatting
    @Test
    fun test55_shareFormatting_matchesSpecification() {
        val result = PercentPartCalculator.calculate(
            partValue = BigDecimal("25"),
            wholeValue = BigDecimal("100"),
            inputType = PercentInputType.NUMBER,
            currency = null
        )
        val expectedShare = """
            PercentPart

            Part: 25
            Whole: 100

            25% of the whole
            75% remaining
        """.trimIndent()
        assertEquals(expectedShare, PercentPartFormatter.formatShareText(result))
    }

    // 56. Quick value presets
    @Test
    fun test56_quickValuePresets_updatePartAndWholeCorrectly() {
        val vm = PercentPartViewModel()
        vm.onQuickPartSelected("75")
        assertEquals("75", vm.uiState.value.partValueInput)
        assertEquals("75%", PercentPartFormatter.formatPercentage(vm.uiState.value.result?.percentage))

        vm.onQuickWholeSelected("1,000")
        assertEquals("1000", vm.uiState.value.wholeValueInput)
        assertEquals("7.5%", PercentPartFormatter.formatPercentage(vm.uiState.value.result?.percentage))

        vm.onCommonExampleSelected("50", "200")
        assertEquals("50", vm.uiState.value.partValueInput)
        assertEquals("200", vm.uiState.value.wholeValueInput)
        assertEquals("25%", PercentPartFormatter.formatPercentage(vm.uiState.value.result?.percentage))
    }

    // 57. Currency switching
    @Test
    fun test57_currencySwitching_adaptsZeroDecimalsAndRecalculates() {
        val vm = PercentPartViewModel()
        vm.onInputTypeChanged(PercentInputType.CURRENCY)
        vm.onCurrencyChanged(CurrencyRepository.USD)
        vm.onPartValueChanged("25.00")
        vm.onWholeValueChanged("100.00")
        assertEquals(CalculationState.Calculated, vm.uiState.value.calculationState)

        // Switch to JPY (0 decimal places) -> "25.00" normalizes to "25"
        vm.onCurrencyChanged(CurrencyRepository.JPY)
        assertEquals("25", vm.uiState.value.partValueInput)
        assertEquals("100", vm.uiState.value.wholeValueInput)
        assertEquals(CalculationState.Calculated, vm.uiState.value.calculationState)
        assertEquals("25%", PercentPartFormatter.formatPercentage(vm.uiState.value.result?.percentage))
    }

    // 58. Rapid calculation & double-save protection + determinism
    @Test
    fun test58_rapidCalculationAndDoubleSaveProtection_isDeterministic() {
        var fakeClock = 5000L
        val vm = PercentPartViewModel(clockProvider = { fakeClock })

        // Rapidly tap SAVE twice at the exact same timestamp -> only 1 entry saved
        assertTrue(vm.saveCurrentResultToHistory())
        assertFalse(vm.saveCurrentResultToHistory())
        assertEquals(1, vm.uiState.value.history.size)

        // Deterministic repeated calculations
        repeat(20) {
            val res = PercentPartCalculator.calculate(BigDecimal("30"), BigDecimal("120"))
            assertEquals(BigDecimal("25"), res.percentage)
            assertEquals(BigDecimal("75"), res.remainingPercentage)
            assertEquals(PercentPartStatus.NORMAL, res.status)
        }
    }
}
