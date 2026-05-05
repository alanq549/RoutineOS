package com.alan.routineos.core.session

import com.alan.routineos.core.datastore.TokenDataStore
import com.alan.routineos.data.remote.auth.AuthApi
import com.alan.routineos.data.remote.auth.Logout.LogoutRequest
import com.alan.routineos.data.remote.auth.refresh.RefreshRequest
import com.alan.routineos.domain.model.AuthSession

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import retrofit2.HttpException

class SessionManager(
    private val tokenDataStore: TokenDataStore,
    private val authApi: AuthApi

) {

    private val _session = MutableStateFlow<AuthSession?>(null)
    val session: StateFlow<AuthSession?> = _session

    // ✅ Acceso síncrono directo — para el interceptor
    fun getAccessToken(): String? = _session.value?.accessToken
    fun getRefreshToken(): String? = _session.value?.refreshToken


    suspend fun loadSession() {
        val access = tokenDataStore.getAccessToken()
        val refresh = tokenDataStore.getRefreshToken()

        if (access == null || refresh == null) {
            _session.value = null
            return
        }

        // ✔ acceso inmediato (offline-first)
        _session.value = AuthSession(access, refresh)

        try {
            val response = authApi.refresh(
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
            // 🔥 SIN INTERNET → NO haces logout
            // simplemente mantienes la sesión actual
        } catch (e: HttpException) {
            // 🔥 ERROR DEL BACKEND (ej: 401)
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
                authApi.logout(LogoutRequest(refresh))
            }
        } catch (e: Exception) {
            // 🔥 ignoras error (offline, etc)
        }

        // 🔴 SIEMPRE limpiar local
        clear()
    }
}
