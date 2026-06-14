package com.alan.routineos.data.repository

import android.util.Log
import com.alan.routineos.data.local.dao.PlanningItemDao
import com.alan.routineos.data.local.entities.PlanningItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanningRepository @Inject constructor(
    private val planningItemDao: PlanningItemDao
) {
    fun getAllPlanningItems(): Flow<List<PlanningItemEntity>> = 
        planningItemDao.getAll().onEach { 
            Log.d("PLANNING_DB_DEBUG", "PLANNING DB LOAD count=${it.size}")
        }

    suspend fun upsertPlanningItem(item: PlanningItemEntity) {
        val isUpdate = false // Room doesn't give this easily on upsert without check
        planningItemDao.upsert(item)
        Log.d("PLANNING_DB_DEBUG", "PLANNING DB UPSERT id=${item.id} status=${item.status}")
    }

    suspend fun deletePlanningItem(id: String) {
        planningItemDao.deleteById(id)
        Log.d("PLANNING_DB_DEBUG", "PLANNING DB DELETE id=$id")
    }
}
