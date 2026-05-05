package com.alan.routineos.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alan.routineos.data.local.dao.UserDao
import com.alan.routineos.data.local.entities.UserEntity

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}