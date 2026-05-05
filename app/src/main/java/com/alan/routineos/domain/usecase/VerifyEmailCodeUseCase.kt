package com.alan.routineos.domain.usecase

import com.alan.routineos.data.repository.AuthRepository
import com.alan.routineos.domain.model.AuthSession

class VerifyEmailCodeUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): Boolean {
        val response = repository.verifyEmailCode(email, code)
        return response.isVerified
    }
}