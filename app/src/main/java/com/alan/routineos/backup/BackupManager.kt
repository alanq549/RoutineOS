package com.alan.routineos.backup

import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupManager: High-level orchestrator for backup and restore operations.
 */
@Singleton
class BackupManager @Inject constructor(
    private val repository: BackupRepository,
    private val jsonDataSource: BackupJsonDataSource,
    private val fileDataSource: BackupFileDataSource
) {

    /**
     * Orchestrates a full backup: Collects data, serializes to JSON, and saves to file.
     */
    suspend fun exportBackup(): BackupExportResult {
        return try {
            Timber.d("Starting backup export...")
            val backup = repository.createBackup()
            val json = jsonDataSource.encodeBackup(backup)
            val file = fileDataSource.writeBackupJson(json)
            Timber.d("Backup exported successfully to: ${file.absolutePath}")
            BackupExportResult(success = true, file = file)
        } catch (e: Exception) {
            Timber.e(e, "Failed to export backup")
            BackupExportResult(success = false, errorMessage = e.message)
        }
    }

    /**
     * Lists all local backup files with basic info and metadata.
     */
    suspend fun listLocalBackups(): List<BackupFileInfo> {
        return fileDataSource.listBackups().map { file ->
            val metadata = try {
                val json = fileDataSource.readBackupJson(file)
                jsonDataSource.readMetadata(json)
            } catch (e: Exception) {
                Timber.w("Failed to read metadata for file: ${file.name}")
                null
            }

            BackupFileInfo(
                file = file,
                name = file.name,
                sizeBytes = file.length(),
                lastModified = file.lastModified(),
                metadata = metadata
            )
        }
    }

    /**
     * Reads metadata for a specific backup file.
     */
    suspend fun readBackupMetadata(file: File): BackupMetadata {
        val json = fileDataSource.readBackupJson(file)
        return jsonDataSource.readMetadata(json)
    }
}
