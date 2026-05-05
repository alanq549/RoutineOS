package com.alan.routineos.data.remote.auth

import com.alan.routineos.data.remote.auth.Logout.LogoutRequest
import com.alan.routineos.data.remote.auth.responses.AuthResponse
import com.alan.routineos.data.remote.auth.login.LoginRequest
import com.alan.routineos.data.remote.auth.login.LoginResponse
import com.alan.routineos.data.remote.auth.refresh.RefreshRequest
import com.alan.routineos.data.remote.auth.register.RegisterRequest
import com.alan.routineos.data.remote.auth.register.RegisterResponse
import com.alan.routineos.data.remote.auth.verify.VerifyEmailCodeRequest
import com.alan.routineos.data.remote.auth.verify.VerifyEmailResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse


    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshRequest
    ): AuthResponse

    @POST("auth/verify-email-code")
    suspend fun verifyEmailCode(
        @Body request: VerifyEmailCodeRequest
    ): VerifyEmailResponse

    @POST( "auth/logout")
    suspend fun logout (
        @Body request: LogoutRequest
    ): LogoutRequest


}