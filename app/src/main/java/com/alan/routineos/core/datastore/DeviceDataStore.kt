package com.alan.routineos.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.deviceDataStore by preferencesDataStore(name = "device_prefs")

@Singleton
class DeviceDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val DEVICE_ID = stringPreferencesKey("device_id")
    }

    suspend fun saveDeviceId(id: String) {
        context.deviceDataStore.edit {
            it[DEVICE_ID] = id
        }
    }

    suspend fun getDeviceId(): String? {
        val prefs = context.deviceDataStore.data.first()
        return prefs[DEVICE_ID]
    }
}
