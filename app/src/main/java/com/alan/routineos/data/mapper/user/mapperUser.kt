package com.alan.routineos.data.mapper.user

import com.alan.routineos.data.local.entities.UserEntity
import com.alan.routineos.data.remote.user.profile.UserResponse
import com.alan.routineos.domain.model.UserProfile

fun UserResponse.toDomain() = UserProfile(
    id = id,
    name = name,
    email = email
)

fun UserProfile.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email
)

fun UserEntity.toDomain() = UserProfile(
    id = id,
    name = name,
    email = email
)