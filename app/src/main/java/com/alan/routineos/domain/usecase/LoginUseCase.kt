package com.alan.routineos.domain.usecase

import com.alan.routineos.data.remote.auth.login.LoginRequest
import com.alan.routineos.data.repository.AuthRepository
import com.alan.routineos.domain.model.AuthSession
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LoginRequest): AuthSession {
        val response = repository.login(request)

        return AuthSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken
        )
    }
}