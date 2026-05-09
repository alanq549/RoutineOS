package com.alan.routineos.core.session

import com.alan.routineos.data.repository.UserRepository
import com.alan.routineos.domain.model.UserProfile
import com.alan.routineos.ui.state.UserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserManager @Inject constructor(
    private val repository: UserRepository,
) {

    private val _state = MutableStateFlow<UserState>(UserState.Idle)
    val state: StateFlow<UserState> = _state

    suspend fun loadLocal() {
        _state.value = UserState.Loading

        val cached = repository.getLocalUser()

        _state.value = cached?.let {
            UserState.Success(it)
        } ?: UserState.Idle
    }

    suspend fun fetchUser() {
        _state.value = UserState.Loading

        try {
            val user = repository.fetchAndCacheUser()


            _state.value = UserState.Success(user)

        } catch (e: Exception) {

            _state.value = UserState.Error("Unexpected error")
        }
    }

    suspend fun syncUser() {
        try {
            val user = repository.fetchAndCacheUser()
            _state.value = UserState.Success(user)
        } catch (e: Exception) {
            // fallback seguro
            val cached = repository.getLocalUser()
            _state.value = cached?.let {
                UserState.Success(it)
            } ?: UserState.Error("No user available")
        }
    }


    suspend fun clear() {
        repository.clear()
        _state.value = UserState.Idle
    }
}