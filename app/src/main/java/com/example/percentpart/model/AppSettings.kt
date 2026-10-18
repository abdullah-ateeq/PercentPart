package com.example.percentpart.model

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val hapticFeedback: Boolean = true,
    val animationsEnabled: Boolean = true,
    val defaultInputType: PercentInputType = PercentInputType.NUMBER,
    val defaultCurrencyCode: String = "USD",
    val customCurrencySymbol: String = "¤",
    val customCurrencyDecimalPlaces: Int = 2,
    val confirmHistoryDelete: Boolean = true,
    val preserveLastCalculation: Boolean = true
)
