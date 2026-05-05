package com.alan.routineos.data.remote.auth.responses

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val status: String
)