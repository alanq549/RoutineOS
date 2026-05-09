package com.alan.routineos.core.session

import com.alan.routineos.core.datastore.TokenDataStore
import com.alan.routineos.data.remote.auth.AuthApi
import com.alan.routineos.data.remote.auth.Logout.LogoutRequest
import com.alan.routineos.data.remote.auth.refresh.RefreshRequest
import com.alan.routineos.domain.model.AuthSession
import dagger.Lazy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val authApi: Lazy<AuthApi>
) {

    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session

    fun getAccessToken(): String? = _session.value?.accessToken
    fun getRefreshToken(): String? = _session.value?.refreshToken

    suspend fun loadSession() {
        val access = tokenDataStore.getAccessToken()
        val refresh = tokenDataStore.getRefreshToken()

        if (access == null || refresh == null) {
            _session.value = null
            return
        }

        _session.value = AuthSession(access, refresh)

        try {
            val response = authApi.get().refresh(
                RefreshRequest(refresh)
            )

            val newSession = AuthSession(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )

            tokenDataStore.saveTokens(
                newSession.accessToken,
                newSession.refreshToken
            )

            _session.value = newSession

        } catch (e: IOException) {
            // Offline - keep current session
        } catch (e: HttpException) {
            clear()
        }
    }

    suspend fun saveSession(session: AuthSession) {
        tokenDataStore.saveTokens(
            access = session.accessToken,
            refresh = session.refreshToken
        )
        _session.value = session
    }

    suspend fun clear() {
        tokenDataStore.clear()
        _session.value = null
    }

    suspend fun logout() {
        val refresh = tokenDataStore.getRefreshToken()

        try {
            if (refresh != null) {
                authApi.get().logout(LogoutRequest(refresh))
            }
        } catch (e: Exception) {
            // Ignore errors (offline, etc)
        }

        clear()
    }
}
