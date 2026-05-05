package com.alan.routineos.core.di

import android.content.Context
import androidx.room.Room
import com.alan.routineos.core.database.AppDatabase
import com.alan.routineos.core.datastore.TokenDataStore
import com.alan.routineos.core.datastore.UserDataStore
import com.alan.routineos.core.network.ApiConfig
import com.alan.routineos.core.network.AuthInterceptor
import com.alan.routineos.data.repository.AuthRepository
import com.alan.routineos.domain.usecase.LoginUseCase
import com.alan.routineos.domain.usecase.RegisterUseCase
import com.alan.routineos.domain.usecase.VerifyEmailCodeUseCase
import com.alan.routineos.core.session.SessionManager
import com.alan.routineos.core.session.UserManager
import com.alan.routineos.data.remote.auth.AuthApi
import com.alan.routineos.data.remote.user.UserApi
import com.alan.routineos.data.repository.UserRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    val db = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_db"
    ).build()

    val userDao = db.userDao()

    val tokenDataStore = TokenDataStore(context)
    val userDataStore = UserDataStore(context)

    val sessionManager = SessionManager(
        tokenDataStore = tokenDataStore,
        authApi = createBaseAuthApi() // 👇 sin interceptor
    )

    private val authInterceptor = AuthInterceptor(
        sessionManager = sessionManager,
        authApi = createBaseAuthApi()
    )

    // 👇 Retrofit limpio y SIN estado global
    private val retrofit = createRetrofit(authInterceptor)

    private val authApi = retrofit.create(AuthApi::class.java)
    private val userApi = retrofit.create(UserApi::class.java)

    val authRepository = AuthRepository(authApi)
    val userRepository = UserRepository(userApi, userDao)

    val userManager = UserManager(userRepository)

    val registerUseCase = RegisterUseCase(authRepository)
    val loginUseCase = LoginUseCase(authRepository)
    val verifyEmailCodeUseCase = VerifyEmailCodeUseCase(authRepository)

    private fun createBaseAuthApi(): AuthApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AuthApi::class.java)
    }

    private fun createRetrofit(interceptor: AuthInterceptor): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}