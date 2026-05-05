package com.alan.routineos.data.repository

import com.alan.routineos.core.datastore.TokenDataStore
import com.alan.routineos.data.remote.auth.AuthApi
import com.alan.routineos.data.remote.auth.responses.AuthResponse
import com.alan.routineos.data.remote.auth.login.LoginRequest
import com.alan.routineos.data.remote.auth.login.LoginResponse
import com.alan.routineos.data.remote.auth.register.RegisterRequest
import com.alan.routineos.data.remote.auth.register.RegisterResponse
import com.alan.routineos.data.remote.auth.verify.VerifyEmailCodeRequest
import com.alan.routineos.data.remote.auth.verify.VerifyEmailResponse
class AuthRepository(
    private val api: AuthApi
) {

    suspend fun register(request: RegisterRequest): RegisterResponse {
        return api.register(request)
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        return api.login(request)
    }

    suspend fun verifyEmailCode(email: String, code: String): VerifyEmailResponse {
        return api.verifyEmailCode(VerifyEmailCodeRequest(email, code))
    }
}