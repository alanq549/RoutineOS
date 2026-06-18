package com.alan.routineos.backup

/**
 * BackupMetadata: Summary information about a backup file.
 */
data class BackupMetadata(
    val version: Int,
    val exportedAt: Long,
    val itemCounts: Map<String, Int>
)
