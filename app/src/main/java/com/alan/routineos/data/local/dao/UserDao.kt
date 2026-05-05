package com.alan.routineos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alan.routineos.data.local.entities.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM UserEntity LIMIT 1")
    suspend fun get(): UserEntity?

    @Query("DELETE FROM UserEntity")
    suspend fun clear()
}