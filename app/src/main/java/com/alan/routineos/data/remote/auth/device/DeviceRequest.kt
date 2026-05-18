package com.alan.routineos.data.remote.auth.device

import com.alan.routineos.data.remote.auth.DevicePlatform

data class DeviceRequest(
    val platform: DevicePlatform,
    val deviceFingerprint: String,
    val deviceName: String?
)