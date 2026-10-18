package com.example.percentpart.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.percentpart.ui.about.AboutScreen
import com.example.percentpart.ui.history.HistoryScreen
import com.example.percentpart.ui.home.HomeScreen
import com.example.percentpart.ui.settings.SettingsScreen
import com.example.percentpart.ui.splash.SplashScreen
import com.example.percentpart.ui.theme.PercentPartTheme
import com.example.percentpart.util.HapticHelper
import kotlinx.coroutines.launch

enum class PercentPartScreen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    Splash(route = "splash", label = "Splash"),
    Home(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate
    ),
    History(
        route = "history",
        label = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    Settings(
        route = "settings",
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    ),
    About(
        route = "about",
        label = "About",
        selectedIcon = Icons.Filled.Info,
        unselectedIcon = Icons.Outlined.Info
    )
}

@Composable
fun PercentPartApp(
    viewModel: PercentPartViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    var isConfirmationDialogActive by remember { mutableStateOf(false) }

    val bottomNavDestinations = remember {
        listOf(
            PercentPartScreen.Home,
            PercentPartScreen.History,
            PercentPartScreen.Settings,
            PercentPartScreen.About
        )
    }

    val showBottomBar = currentRoute != null &&
        currentRoute != PercentPartScreen.Splash.route &&
        !isConfirmationDialogActive

    PercentPartTheme(
        themeMode = uiState.settings.themeMode,
        dynamicColor = uiState.settings.dynamicColor
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavDestinations.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        HapticHelper.performClickHaptic(
                                            view,
                                            uiState.settings.hapticFeedback
                                        )
                                        navController.navigate(screen.route) {
                                            popUpTo(PercentPartScreen.Home.route) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    val icon = if (isSelected) {
                                        screen.selectedIcon
                                    } else {
                                        screen.unselectedIcon
                                    }
                                    if (icon != null) {
                                        Icon(imageVector = icon, contentDescription = screen.label)
                                    }
                                },
                                label = { Text(screen.label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = PercentPartScreen.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(PercentPartScreen.Splash.route) {
                    SplashScreen(
                        animationsEnabled = uiState.settings.animationsEnabled,
                        onSplashFinished = {
                            navController.navigate(PercentPartScreen.Home.route) {
                                popUpTo(PercentPartScreen.Splash.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(PercentPartScreen.Home.route) {
                    HomeScreen(
                        uiState = uiState,
                        onPartChanged = viewModel::onPartValueChanged,
                        onWholeChanged = viewModel::onWholeValueChanged,
                        onInputTypeChanged = viewModel::onInputTypeChanged,
                        onCurrencyChanged = viewModel::onCurrencyChanged,
                        onCustomCurrencyUpdate = viewModel::updateCustomCurrency,
                        onQuickPartSelected = viewModel::onQuickPartSelected,
                        onQuickWholeSelected = viewModel::onQuickWholeSelected,
                        onCommonExampleSelected = viewModel::onCommonExampleSelected,
                        onCalculateClicked = viewModel::calculate,
                        onSwapClicked = viewModel::swapValues,
                        onResetClicked = viewModel::reset,
                        onSaveClicked = viewModel::saveCurrentResultToHistory,
                        onRestoreHistoryItem = { item ->
                            viewModel.restoreHistoryItem(item)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Restored calculation")
                            }
                        },
                        onNavigateToHistory = {
                            navController.navigate(PercentPartScreen.History.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onShowSnackbar = { msg ->
                            coroutineScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                }

                composable(PercentPartScreen.History.route) {
                    HistoryScreen(
                        history = uiState.history,
                        confirmDelete = uiState.settings.confirmHistoryDelete,
                        hapticsEnabled = uiState.settings.hapticFeedback,
                        onSelectHistoryItem = { item ->
                            viewModel.restoreHistoryItem(item)
                            navController.navigate(PercentPartScreen.Home.route) {
                                popUpTo(PercentPartScreen.Home.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        onDeleteHistoryItem = viewModel::deleteHistoryItem,
                        onClearAllHistory = viewModel::clearHistory,
                        onDialogVisibilityChanged = { visible ->
                            isConfirmationDialogActive = visible
                        }
                    )
                }

                composable(PercentPartScreen.Settings.route) {
                    SettingsScreen(
                        settings = uiState.settings,
                        historyCount = uiState.history.size,
                        onThemeModeChange = viewModel::updateThemeMode,
                        onDynamicColorChange = viewModel::updateDynamicColor,
                        onHapticsChange = viewModel::updateHapticFeedback,
                        onAnimationsChange = viewModel::updateAnimationsEnabled,
                        onDefaultInputTypeChange = viewModel::updateDefaultInputType,
                        onDefaultCurrencyChange = viewModel::updateDefaultCurrency,
                        onCustomCurrencyUpdate = viewModel::updateCustomCurrency,
                        onConfirmHistoryDeleteChange = viewModel::updateConfirmHistoryDelete,
                        onPreserveLastCalcChange = viewModel::updatePreserveLastCalculation,
                        onClearHistory = {
                            viewModel.clearHistory()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("History cleared")
                            }
                        },
                        onResetSettings = {
                            viewModel.resetSettings()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Settings restored to defaults")
                            }
                        },
                        onDialogVisibilityChanged = { visible ->
                            isConfirmationDialogActive = visible
                        }
                    )
                }

                composable(PercentPartScreen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
