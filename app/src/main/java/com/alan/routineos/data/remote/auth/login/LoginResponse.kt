package com.alan.routineos.data.remote.auth.login

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val status: String
)