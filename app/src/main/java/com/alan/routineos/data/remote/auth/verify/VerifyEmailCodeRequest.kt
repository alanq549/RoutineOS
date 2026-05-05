package com.alan.routineos.data.remote.auth.verify

data class VerifyEmailCodeRequest(
    val email: String,
    val code: String
)