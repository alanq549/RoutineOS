package com.alan.routineos.backup

import com.alan.routineos.data.local.dao.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val templateDao: TemplateDao,
    private val nodeDao: NodeDao,
    private val schemaDao: MetadataSchemaDao,
    private val fieldValueDao: FieldValueDao,
    private val dayInstanceDao: DayInstanceDao,
    private val nodeOverrideDao: NodeOverrideDao,
    private val executionFieldValueDao: ExecutionFieldValueDao,
    private val planningItemDao: PlanningItemDao,
    private val scheduleExceptionDao: ScheduleExceptionDao
) {

    suspend fun createBackup(): RoutineOsBackup {
        val templates = templateDao.getAll().first()
        
        // Combine template nodes and instance nodes
        val nodes = combine(
            nodeDao.getAllTemplateNodes(),
            nodeDao.getAllInstanceNodes()
        ) { templates, instances -> templates + instances }.first()
        
        val metadataSchemas = schemaDao.getAll().first()
        val fieldValues = fieldValueDao.getAll().first()
        val dayInstances = dayInstanceDao.getInRange(0, Long.MAX_VALUE).first()
        val nodeOverrides = nodeOverrideDao.getAll().first()
        val executionFieldValues = executionFieldValueDao.getAll().first()
        val planningItems = planningItemDao.getAll().first()
        val scheduleExceptions = scheduleExceptionDao.getAllSync()

        return RoutineOsBackup(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            templates = templates,
            nodes = nodes,
            metadataSchemas = metadataSchemas,
            fieldValues = fieldValues,
            dayInstances = dayInstances,
            nodeOverrides = nodeOverrides,
            executionFieldValues = executionFieldValues,
            planningItems = planningItems,
            scheduleExceptions = scheduleExceptions
        )
    }

    suspend fun getBackupMetadata(): BackupMetadata {
        val backup = createBackup()
        val counts = mapOf(
            "templates" to backup.templates.size,
            "nodes" to backup.nodes.size,
            "metadataSchemas" to backup.metadataSchemas.size,
            "fieldValues" to backup.fieldValues.size,
            "dayInstances" to backup.dayInstances.size,
            "nodeOverrides" to backup.nodeOverrides.size,
            "executionFieldValues" to backup.executionFieldValues.size,
            "planningItems" to backup.planningItems.size,
            "scheduleExceptions" to backup.scheduleExceptions.size
        )

        return BackupMetadata(
            version = backup.version,
            exportedAt = backup.exportedAt,
            itemCounts = counts
        )
    }
}
