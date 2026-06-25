# Expense Tracker

A native Android expense tracking app built with **Kotlin**, **Jetpack Compose**, and **Room**.

## Features

- Add, edit, and delete expenses
- Categorize spending (Food, Transport, Shopping, Bills, Entertainment, Health, Other)
- **Monthly budgets** — total and per-category limits with progress tracking
- **Charts** — pie chart by category and 6-month bar chart
- **CSV export** — share all expenses via the system share sheet
- **Recurring expenses** — weekly, monthly, or yearly; auto-logged when due
- Local SQLite storage via Room (works offline)
- Material 3 UI with bottom navigation and light/dark theme

## Requirements

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 35
- Min SDK 26 (Android 8.0+)

## Getting Started

1. Open the project folder in Android Studio
2. Let Gradle sync complete
3. Run on an emulator or physical device (Run ▶)

Or from the command line:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## Project Structure

```
app/src/main/java/com/expensetracker/app/
├── data/           # Room entities, DAO, database, repository
├── ui/
│   ├── screens/    # Home and Add/Edit screens
│   └── theme/      # Material 3 theme
├── viewmodel/      # ExpenseViewModel
├── MainActivity.kt
└── ExpenseTrackerApp.kt
```

## Tech Stack

| Layer      | Technology              |
|-----------|-------------------------|
| UI        | Jetpack Compose, Material 3 |
| Navigation| Navigation Compose      |
| Database  | Room                    |
| Architecture | MVVM + Repository    |
