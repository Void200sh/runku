package com.void200.runku.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.void200.runku.ui.theme.RunkuTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "runku_settings")

/** User settings: theme, dark mode, map mode, HR source, units. */
class SettingsStore(private val context: Context) {

    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_DARK = stringPreferencesKey("dark_mode")   // "auto" | "light" | "dark"
    private val KEY_MAP = stringPreferencesKey("map_mode")      // "canvas" | "tiles"
    private val KEY_HR = stringPreferencesKey("hr_source")      // "ble" | "camera" | "off"
    private val KEY_UNIT = stringPreferencesKey("unit")         // "km" | "mi"
    private val KEY_ONBOARD = booleanPreferencesKey("onboarded")

    data class Snapshot(
        val theme: RunkuTheme = RunkuTheme.PIXEL,
        val darkMode: String = "auto",
        val mapMode: String = "canvas",
        val hrSource: String = "camera",
        val unit: String = "km",
        val onboarded: Boolean = false,
    )

    val flow: Flow<Snapshot> = context.dataStore.data.map { p ->
        Snapshot(
            theme = RunkuTheme.fromId(p[KEY_THEME] ?: "pixel"),
            darkMode = p[KEY_DARK] ?: "auto",
            mapMode = p[KEY_MAP] ?: "canvas",
            hrSource = p[KEY_HR] ?: "camera",
            unit = p[KEY_UNIT] ?: "km",
            onboarded = p[KEY_ONBOARD] ?: false,
        )
    }

    suspend fun setTheme(t: RunkuTheme) = context.dataStore.edit { it[KEY_THEME] = t.id }
    suspend fun setDarkMode(m: String) = context.dataStore.edit { it[KEY_DARK] = m }
    suspend fun setMapMode(m: String) = context.dataStore.edit { it[KEY_MAP] = m }
    suspend fun setHrSource(s: String) = context.dataStore.edit { it[KEY_HR] = s }
    suspend fun setUnit(u: String) = context.dataStore.edit { it[KEY_UNIT] = u }
    suspend fun setOnboarded() = context.dataStore.edit { it[KEY_ONBOARD] = true }
}
