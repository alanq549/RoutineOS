package com.alan.routineos.data.remote.auth.register

import com.alan.routineos.data.remote.auth.device.DeviceRequest

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val timezone: String?,
    val device: DeviceRequest
)