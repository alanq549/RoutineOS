package com.alan.routineos.data.remote.auth.verify

data class VerifyEmailResponse(
    val status: String,
    val isVerified: Boolean
)