package com.alan.routineos.core.session

import com.alan.routineos.data.repository.UserRepository
import com.alan.routineos.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserManager: Singleton source of truth for the current user profile.
 * Crucial for keeping Account and Auth states in sync.
 */
@Singleton
class UserManager @Inject constructor(
    private val repository: UserRepository,
) {

    private val _user = MutableStateFlow<UserProfile?>(null)
    val user: StateFlow<UserProfile?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadLocal() {
        if (_user.value != null) return // Already loaded

        _isLoading.value = true
        _error.value = null
        val cached = repository.getLocalUser()
        _user.value = cached
        _isLoading.value = false
    }

    suspend fun fetchUser() {
        _isLoading.value = true
        _error.value = null
        try {
            val user = repository.fetchAndCacheUser()
            _user.value = user
        } catch (e: Exception) {
            _error.value = e.message ?: "Unexpected error"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun syncUser() {
        _isLoading.value = true
        _error.value = null
        try {
            val user = repository.fetchAndCacheUser()
            _user.value = user
        } catch (e: Exception) {
            val cached = repository.getLocalUser()
            if (cached != null) {
                _user.value = cached
            } else {
                _error.value = e.message ?: "No user available"
            }
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun clear() {
        repository.clear()
        _user.value = null
        _error.value = null
        _isLoading.value = false
    }
}