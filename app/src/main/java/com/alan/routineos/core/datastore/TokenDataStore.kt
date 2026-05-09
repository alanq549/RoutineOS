package com.alan.routineos.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = access
            prefs[REFRESH_TOKEN] = refresh
        }
    }

    suspend fun getAccessToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[ACCESS_TOKEN]
    }

    suspend fun getRefreshToken(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[REFRESH_TOKEN]
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
