package com.example.percentpart.model

data class PercentPartUiState(
    val partValueInput: String = "25",
    val wholeValueInput: String = "100",
    val inputType: PercentInputType = PercentInputType.NUMBER,
    val currency: CurrencyOption? = null,
    val result: PercentPartResult? = null,
    val history: List<PercentPartHistory> = emptyList(),
    val errorMessage: String? = null,
    val isCalculating: Boolean = false,
    val calculationState: CalculationState = CalculationState.Idle,
    val settings: AppSettings = AppSettings(),
    val partMinorUnits: Long? = null,
    val wholeMinorUnits: Long? = null
)
