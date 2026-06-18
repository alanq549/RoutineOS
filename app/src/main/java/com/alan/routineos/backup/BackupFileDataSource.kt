package com.alan.routineos.backup

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BackupFileDataSource: Responsible for saving and reading backup files from local storage.
 */
@Singleton
class BackupFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Writes the backup JSON string to a file in the app's private internal storage.
     * Filename format: routineos-backup-{timestamp}.json
     * @return The generated File object.
     */
    suspend fun writeBackupJson(json: String): File = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val fileName = "routineos-backup-$timestamp.json"
        
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        val file = File(backupDir, fileName)
        
        try {
            FileOutputStream(file).use { output ->
                output.write(json.toByteArray())
            }
            file
        } catch (e: Exception) {
            throw IllegalStateException("Failed to write backup file: ${e.message}", e)
        }
    }

    /**
     * Reads a backup file and returns its content as a JSON string.
     */
    suspend fun readBackupJson(file: File): String = withContext(Dispatchers.IO) {
        try {
            file.readText()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read backup file: ${e.message}", e)
        }
    }

    /**
     * Lists all backup files in the app's private internal storage.
     */
    fun listBackups(): List<File> {
        val backupDir = File(context.filesDir, "backups")
        return if (backupDir.exists()) {
            backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }
}
