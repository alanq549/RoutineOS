package com.alan.routineos.core.datastore

import android.content.Context
import com.alan.routineos.domain.model.UserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    // simplificado (puedes usar Preferences o Proto)
    private var cachedUser: UserProfile? = null

    suspend fun saveUser(user: UserProfile) {
        cachedUser = user
    }

    suspend fun getUser(): UserProfile? {
        return cachedUser
    }

    suspend fun clear() {
        cachedUser = null
    }
}
