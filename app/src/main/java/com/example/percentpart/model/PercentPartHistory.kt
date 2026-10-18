package com.example.percentpart.model

data class PercentPartHistory(
    val id: String,
    val partValue: String,
    val wholeValue: String,
    val percentage: String,
    val remainingPercentage: String?,
    val status: PercentPartStatus,
    val inputType: PercentInputType,
    val currencyCode: String?,
    val currencySymbol: String?,
    val calculatedAt: Long,
    val currencyDecimalPlaces: Int? = null
)
