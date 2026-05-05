package com.alan.routineos.core.datastore

import android.content.Context
import com.alan.routineos.domain.model.UserProfile

class UserDataStore(context: Context) {

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