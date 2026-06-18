package com.alan.routineos.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupJsonDataSource: Responsible for serializing and deserializing backup data.
 */
@Singleton
class BackupJsonDataSource @Inject constructor() {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Converts a RoutineOsBackup object into a formatted JSON string.
     */
    fun encodeBackup(backup: RoutineOsBackup): String {
        return gson.toJson(backup)
    }

    /**
     * Converts a JSON string back into a RoutineOsBackup object.
     * @throws Exception if JSON is invalid or incompatible.
     */
    fun decodeBackup(json: String): RoutineOsBackup {
        return try {
            gson.fromJson(json, RoutineOsBackup::class.java)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid backup format", e)
        }
    }

    /**
     * Reads metadata from a backup JSON string without performing a full restoration.
     */
    fun readMetadata(json: String): BackupMetadata {
        val backup = decodeBackup(json)
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
