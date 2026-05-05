package com.alan.routineos.domain.usecase

import com.alan.routineos.data.remote.auth.register.RegisterRequest
import com.alan.routineos.data.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: RegisterRequest) {
        repository.register(request)
        // no retorna sesión
    }
}