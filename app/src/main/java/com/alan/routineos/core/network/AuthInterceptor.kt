package com.alan.routineos.core.network

import com.alan.routineos.core.session.SessionManager
import com.alan.routineos.data.remote.auth.AuthApi
import com.alan.routineos.data.remote.auth.refresh.RefreshRequest
import com.alan.routineos.domain.model.AuthSession
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val authApi: Lazy<AuthApi>
) : Interceptor {

    private val mutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val path = request.url().encodedPath()

        if (
            path.contains("/auth/refresh") ||
            path.contains("/auth/login") ||
            path.contains("/auth/logout") ||
            request.header("X-Retry") != null
        ) {
            return chain.proceed(request)
        }

        val accessToken = sessionManager.getAccessToken()

        if (accessToken != null) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        val response = chain.proceed(request)

        if (response.code() != 401) return response

        response.close()

        val newSession = runBlocking {
            mutex.withLock {
                try {
                    val refreshToken = sessionManager.getRefreshToken()
                        ?: return@withLock null

                    val refreshResponse = authApi.get().refresh(RefreshRequest(refreshToken))

                    val updated = AuthSession(
                        accessToken = refreshResponse.accessToken,
                        refreshToken = refreshResponse.refreshToken
                    )

                    sessionManager.saveSession(updated)
                    updated

                } catch (e: IOException) {
                    null
                } catch (e: HttpException) {
                    if (e.code() == 401) sessionManager.clear()
                    null
                } catch (e: Exception) {
                    null
                }
            }
        }

        if (newSession == null) throw IOException("Token refresh failed")

        val newRequest = request.newBuilder()
            .header("Authorization", "Bearer ${newSession.accessToken}")
            .header("X-Retry", "true")
            .build()

        return chain.proceed(newRequest)
    }
}
