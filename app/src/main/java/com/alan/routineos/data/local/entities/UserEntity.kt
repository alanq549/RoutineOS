package com.alan.routineos.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String
)