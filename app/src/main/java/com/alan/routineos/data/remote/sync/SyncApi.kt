package com.alan.routineos.data.remote.sync

import retrofit2.http.Body
import retrofit2.http.POST

interface SyncApi {
    @POST("sync/push")
    suspend fun push(@Body request: SyncPushRequest): SyncResponse

    @POST("sync/pull")
    suspend fun pull(@Body lastSyncTimestamp: Long): SyncResponse
}
