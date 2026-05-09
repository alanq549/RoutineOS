package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.UserDao
import com.alan.routineos.data.mapper.user.toDomain
import com.alan.routineos.data.mapper.user.toEntity
import com.alan.routineos.data.remote.user.UserApi
import com.alan.routineos.domain.model.UserProfile
import javax.inject.Inject

class UserRepository  @Inject constructor(
    private val api: UserApi,
    private val dao: UserDao
) {

    suspend fun fetchAndCacheUser(): UserProfile {
        val remote = api.getMe().toDomain()
        dao.insert(remote.toEntity())
        return remote
    }

    suspend fun getLocalUser(): UserProfile? {
        return dao.get()?.toDomain()
    }

    suspend fun clear() {
        dao.clear()
    }
}