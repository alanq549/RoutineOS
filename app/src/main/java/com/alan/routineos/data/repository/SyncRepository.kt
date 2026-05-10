package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.SyncStatus
import com.alan.routineos.data.remote.sync.SyncApi
import com.alan.routineos.data.remote.sync.SyncPushRequest
import com.alan.routineos.data.remote.sync.SyncResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val syncDao: SyncDao,
    private val nodeDao: NodeDao,
    private val nodeTypeDao: NodeTypeDao,
    private val schemaDao: MetadataSchemaDao,
    private val fieldValueDao: FieldValueDao,
    private val templateDao: TemplateDao,
    private val dayInstanceDao: DayInstanceDao,
    private val scheduleDao: ScheduleDao,
    private val exceptionDao: ScheduleExceptionDao,
    private val nodeOverrideDao: NodeOverrideDao,
    private val syncApi: SyncApi
) {
    suspend fun sync() {
        try {
            // 1. PUSH: Enviar cambios locales pendientes al servidor
            val pendingOverrides = syncDao.getPendingOverrides()
            val pushRequest = SyncPushRequest(
                nodes = syncDao.getPendingNodes(),
                nodeTypes = syncDao.getPendingNodeTypes(),
                schemas = syncDao.getPendingSchemas(),
                values = syncDao.getPendingFieldValues(),
                templates = syncDao.getPendingTemplates(),
                schedules = syncDao.getPendingSchedules(),
                instances = syncDao.getPendingInstances(),
                exceptions = syncDao.getPendingExceptions(),
                overrides = pendingOverrides
            )

            if (isEmpty(pushRequest)) {
                Timber.d("Sync: No hay cambios pendientes para subir.")
                // Aún así podríamos querer hacer un PULL si el servidor tiene algo nuevo.
                // pull() // TODO: implementar pull por separado si es necesario
            }

            val response = syncApi.push(pushRequest)

            if (response.success) {
                // 2. Marcar como sincronizado localmente
                syncDao.markAsSynced(
                    nodes = pushRequest.nodes.map { it.id },
                    types = pushRequest.nodeTypes.map { it.id },
                    schemas = pushRequest.schemas.map { it.id },
                    values = pushRequest.values.map { it.id }, // Ojo: id aquí es UUID
                    templates = pushRequest.templates.map { it.id },
                    schedules = pushRequest.schedules.map { it.id },
                    instances = pushRequest.instances.map { it.id },
                    exceptions = pushRequest.exceptions.map { it.id },
                    overrides = pendingOverrides.map { it.id }
                )
                
                // 3. PULL: Procesar datos recibidos del servidor
                processPullData(response)
                
                Timber.d("Sync: Sincronización (Push/Pull) completada con éxito.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Sync: Fallo en la sincronización")
            throw e
        }
    }

    private fun isEmpty(request: SyncPushRequest): Boolean {
        return request.nodes.isEmpty() && request.nodeTypes.isEmpty() && 
               request.schemas.isEmpty() && request.values.isEmpty() && 
               request.templates.isEmpty() && request.schedules.isEmpty() && 
               request.instances.isEmpty() && request.exceptions.isEmpty() &&
               request.overrides.isEmpty()
    }

    private suspend fun processPullData(response: SyncResponse) {
        // En una implementación real, compararíamos versiones (Last-write-wins)
        // Por simplicidad en esta fase, hacemos upsert de todo lo recibido con status SYNCED
        response.nodeTypes.forEach { nodeTypeDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.schemas.forEach { schemaDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.templates.forEach { templateDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.nodes.forEach { nodeDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.values.forEach { fieldValueDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.schedules.forEach { scheduleDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.instances.forEach { dayInstanceDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.exceptions.forEach { exceptionDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
        response.overrides.forEach { nodeOverrideDao.upsert(it.copy(syncStatus = SyncStatus.SYNCED)) }
    }
}
