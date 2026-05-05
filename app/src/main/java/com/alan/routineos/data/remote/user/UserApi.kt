package com.alan.routineos.data.remote.user

import com.alan.routineos.data.remote.user.profile.UserResponse
import retrofit2.http.GET

interface UserApi {
    @GET("/user/me")
    suspend fun getMe(): UserResponse

}