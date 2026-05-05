package com.alan.routineos.data.remote.auth.device

import com.alan.routineos.data.remote.auth.DevicePlatform

data class DeviceRequest(
    val platform: DevicePlatform, // "ANDROID"
    val deviceName: String?,
    val deviceFingerprint: String?
)