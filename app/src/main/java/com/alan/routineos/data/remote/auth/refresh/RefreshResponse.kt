package com.alan.routineos.data.remote.auth.refresh

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)