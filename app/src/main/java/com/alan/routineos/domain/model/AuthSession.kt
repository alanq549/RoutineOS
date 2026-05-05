package com.alan.routineos.domain.model


data class AuthSession(
    val accessToken: String,
    val refreshToken: String
)

