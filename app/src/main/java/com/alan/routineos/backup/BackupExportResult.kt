package com.alan.routineos.backup

import java.io.File

/**
 * BackupExportResult: Outcome of an export operation.
 */
data class BackupExportResult(
    val success: Boolean,
    val file: File? = null,
    val errorMessage: String? = null
)
