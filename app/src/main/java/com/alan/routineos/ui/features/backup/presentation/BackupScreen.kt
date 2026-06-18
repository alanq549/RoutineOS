package com.alan.routineos.ui.features.backup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.backup.BackupFileInfo
import com.alan.routineos.ui.features.backup.viewmodel.BackupViewModel
import com.alan.routineos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ColorBg,
        topBar = {
            BackupTopBar(onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Main Action Card
            Surface(
                color = ColorSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ColorExec.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Backup, null, tint = ColorExec, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Backup Local", style = TitleNode.copy(fontSize = 16.sp), color = ColorText)
                            Text("Exporta tus datos a un archivo JSON", style = MetaMono.copy(fontSize = 10.sp), color = ColorTextDim)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.isExporting) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = ColorExec,
                            trackColor = ColorBorder.copy(alpha = 0.3f)
                        )
                    } else {
                        Button(
                            onClick = { viewModel.exportBackup() },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CREAR RESPALDO AHORA", style = MetaMono.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    uiState.lastExportResult?.let { result ->
                        if (result.success) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = ColorExec, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Exportado: ${result.file?.name}",
                                    style = MetaMono.copy(fontSize = 10.sp),
                                    color = ColorExec
                                )
                            }
                        }
                    }

                    uiState.errorMessage?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(error, color = Color.Red, style = MetaMono.copy(fontSize = 10.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "ARCHIVOS LOCALES",
                style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 2.sp),
                color = ColorTextMuted
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.backups.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay backups locales", style = MetaMono, color = ColorTextDim)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.backups) { backup ->
                        BackupFileRow(backup)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupFileRow(info: BackupFileInfo) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(info.lastModified))
    val sizeStr = String.format("%.2f KB", info.sizeBytes / 1024f)

    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Storage, null, tint = ColorTextDim, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(info.name, style = TitleNode.copy(fontSize = 14.sp), color = ColorText)
                Text("$dateStr • $sizeStr", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            }
        }
    }
}

@Composable
private fun BackupTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ColorSurface, CircleShape)
                .border(0.5.dp, ColorBorder, CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = ColorTextDim,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "BACKUP Y RESTAURACIÓN",
            style = MetaMono.copy(fontSize = 11.sp, letterSpacing = 2.sp),
            color = ColorTextDim
        )
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(32.dp))
    }
}
