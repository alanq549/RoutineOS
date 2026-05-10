package com.alan.routineos.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alan.routineos.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker: Starting synchronization...")
        return try {
            syncRepository.sync()
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker: Synchronization failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
