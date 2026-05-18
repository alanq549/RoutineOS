package com.alan.routineos.core.util

import android.os.Build
import com.alan.routineos.core.datastore.DeviceDataStore
import com.alan.routineos.data.remote.auth.DevicePlatform
import com.alan.routineos.data.remote.auth.device.DeviceRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface DeviceInfoProvider {
    fun getTimezone(): String
    fun getDeviceName(): String
    fun getPlatform(): DevicePlatform
    suspend fun getDeviceFingerprint(): String
    suspend fun getDeviceRequest(): DeviceRequest
}

@Singleton
class DeviceInfoProviderImpl @Inject constructor(
    private val deviceDataStore: DeviceDataStore
) : DeviceInfoProvider {

    private val mutex = Mutex()

    override fun getTimezone(): String {
        return TimeZone.getDefault().id
    }

    override fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }

    override fun getPlatform(): DevicePlatform {
        return DevicePlatform.ANDROID
    }

    override suspend fun getDeviceFingerprint(): String = mutex.withLock {
        val existingId = deviceDataStore.getDeviceId()
        if (existingId != null) {
            existingId
        } else {
            val newId = UUID.randomUUID().toString()
            deviceDataStore.saveDeviceId(newId)
            newId
        }
    }

    override suspend fun getDeviceRequest(): DeviceRequest {
        return DeviceRequest(
            platform = getPlatform(),
            deviceFingerprint = getDeviceFingerprint(),
            deviceName = getDeviceName()
        )
    }
}
