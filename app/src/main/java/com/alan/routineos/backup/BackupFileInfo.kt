package com.alan.routineos.backup

import java.io.File

/**
 * BackupFileInfo: Information about a local backup file.
 */
data class BackupFileInfo(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val metadata: BackupMetadata? = null
)
