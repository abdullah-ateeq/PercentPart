# PercentPart 📊

**PercentPart** is an intuitive, offline-first Android application built with **Kotlin** and **Jetpack Compose**. It allows users to quickly calculate what percentage a specific part represents of a whole (`(Part ÷ Whole) × 100`), with support for raw numbers, currency values, custom precision, value swapping, and calculation history.

---

## ✨ Features

- 🔢 **Number & Currency Modes**: Seamlessly calculate percentages for plain numbers or monetary amounts.
- 💱 **Multi-Currency Support**: Supports major global currencies (USD, EUR, GBP, JPY, CAD, AUD, INR, etc.) and custom currency symbols.
- 🔄 **Value Swapping**: Swap "Part" and "Whole" values instantly with a single tap.
- ⏱️ **Remaining Percentage**: Automatically computes the remaining balance (`100% - Percentage`).
- 📜 **Calculation History**: Automatically or manually save past calculations, restore previous entries, and manage history.
- 📋 **Copy & Share**: Copy calculated percentages, formatted text, or raw values to the clipboard, or share them directly with other apps.
- 🎨 **Material 3 Customization**: Dynamic Light & Dark themes, Dynamic Color (Material You) support, and adjustable settings.
- 📳 **Haptic Feedback**: Tactile responses on button taps and screen transitions (can be toggled on/off).
- 🔒 **100% Offline & Private**: Zero network permissions required. No tracking, no ads, no cloud sync, and all data stays strictly on device.

---

## 🛠 Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: Clean Architecture / MVVM Pattern (ViewModel, Repositories, Domain models, and pure Calculators/Validators/Formatters)
- **Async & Reactive**: Kotlin Coroutines & `StateFlow` / `collectAsStateWithLifecycle`
- **Navigation**: Navigation Compose
- **Local Persistence**: SharedPreferences via Custom Repositories
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`) & Gradle Kotlin DSL (`.gradle.kts`)
- **Compatibility**: Minimum SDK 26 (Android 8.0), Target SDK 37

---

## 📐 Math & Logic

The calculation engine uses `BigDecimal` with an explicit internal precision context (`MathContext(34, RoundingMode.HALF_UP)`):

$$\text{Percentage} = \left(\frac{\text{Part}}{\text{Whole}}\right) \times 100$$

$$\text{Remaining Percentage} = 100 - \text{Percentage}$$

Edge cases handled:
- Division by Zero: Flagged as `UNDEFINED`.
- Part Exceeds Whole (`Part > Whole`): Evaluated smoothly with a visual indicator.
- Currency Minor Units: Preserves precise cents/minor units according to standard currency precision.

---

## 📱 Screenshots & Screens

- **Home Screen**: Main calculator interface with quick selections, value swap, reset, copy/share, and instant calculation results.
- **History Screen**: Detailed record of past calculations with option to restore or delete records.
- **Settings Screen**: Customization for theme mode, dynamic color, haptics, animations, default input types, and default currency.
- **About Screen**: Information about the app features, privacy statement, and version metadata.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (or newer)
- JDK 17 or higher
- Android SDK 37

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/PercentPart.git
   cd PercentPart
   ```

2. Open the project in **Android Studio**.

3. Build and run the app:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run unit tests:
   ```bash
   ./gradlew test
   ```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
