package com.alan.routineos.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        private val SHOW_HEATMAP = booleanPreferencesKey("show_heatmap")
        private val SHOW_INSIGHTS = booleanPreferencesKey("show_insights")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }

    val isRemindersEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[REMINDERS_ENABLED] ?: true }

    val isShowHeatmapEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SHOW_HEATMAP] ?: true }

    val isShowInsightsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SHOW_INSIGHTS] ?: true }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[REMINDERS_ENABLED] = enabled }
    }

    suspend fun setShowHeatmapEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[SHOW_HEATMAP] = enabled }
    }

    suspend fun setShowInsightsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[SHOW_INSIGHTS] = enabled }
    }
}
