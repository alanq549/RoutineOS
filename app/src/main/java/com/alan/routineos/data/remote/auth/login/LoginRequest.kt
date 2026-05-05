package com.alan.routineos.data.remote.auth.login

import com.alan.routineos.data.remote.auth.device.DeviceRequest

data class LoginRequest(
    val email: String,
    val password: String,
    val device: DeviceRequest
)