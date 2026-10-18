package com.example.percentpart

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.percentpart.repository.PercentPartHistoryRepository
import com.example.percentpart.repository.SettingsRepository
import com.example.percentpart.ui.PercentPartApp
import com.example.percentpart.ui.PercentPartViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences = applicationContext.getSharedPreferences(
            "percent_part_preferences",
            Context.MODE_PRIVATE
        )
        val settingsRepository = SettingsRepository(sharedPreferences)
        val historyRepository = PercentPartHistoryRepository(sharedPreferences)

        setContent {
            val percentPartViewModel: PercentPartViewModel = viewModel(
                factory = PercentPartViewModel.provideFactory(
                    settingsRepository = settingsRepository,
                    historyRepository = historyRepository
                )
            )
            PercentPartApp(viewModel = percentPartViewModel)
        }
    }
}