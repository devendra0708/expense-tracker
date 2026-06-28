package com.expensetracker.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserSettings(
    val name: String = "",
    val email: String = "",
    val currency: SupportedCurrency = SupportedCurrency.USD
)

enum class SupportedCurrency(
    val code: String,
    val symbol: String,
    val label: String
) {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    INR("INR", "₹", "Indian Rupee"),
    JPY("JPY", "¥", "Japanese Yen")
}

class UserSettingsRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(readSettings())

    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    fun saveSettings(settings: UserSettings) {
        val normalizedSettings = settings.copy(
            name = settings.name.trim(),
            email = settings.email.trim()
        )
        preferences.edit()
            .putString(KEY_NAME, normalizedSettings.name)
            .putString(KEY_EMAIL, normalizedSettings.email)
            .putString(KEY_CURRENCY, normalizedSettings.currency.code)
            .apply()
        _settings.value = normalizedSettings
    }

    private fun readSettings(): UserSettings {
        val currencyCode = preferences.getString(KEY_CURRENCY, SupportedCurrency.USD.code)
        return UserSettings(
            name = preferences.getString(KEY_NAME, "").orEmpty(),
            email = preferences.getString(KEY_EMAIL, "").orEmpty(),
            currency = SupportedCurrency.entries.firstOrNull { it.code == currencyCode }
                ?: SupportedCurrency.USD
        )
    }

    private companion object {
        const val PREFS_NAME = "user_settings"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_CURRENCY = "currency"
    }
}
