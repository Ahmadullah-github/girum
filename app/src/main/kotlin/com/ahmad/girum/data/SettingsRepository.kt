package com.ahmad.girum.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "girum_settings")

enum class AppLanguage {
    DARI,
    ENGLISH,
}

class SettingsRepository(context: Context) {
    private val appContext = context.applicationContext
    private val languageKey = stringPreferencesKey("language")

    val language: Flow<AppLanguage> = appContext.settingsDataStore.data.map { preferences ->
        when (preferences[languageKey]) {
            AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
            else -> AppLanguage.DARI
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        appContext.settingsDataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
    }
}
