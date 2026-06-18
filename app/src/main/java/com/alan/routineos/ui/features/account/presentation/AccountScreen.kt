package com.alan.routineos.ui.features.account.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.account.state.UserState
import com.alan.routineos.ui.features.account.viewmodel.UserViewModel
import com.alan.routineos.ui.features.account.viewmodel.SettingsViewModel
import com.alan.routineos.ui.theme.*

/**
 * SETTINGS CENTER (formerly Account Screen)
 * Centralized configuration for the RoutineOS ecosystem.
 */
@Composable
fun AccountScreen(
    userViewModel: UserViewModel,
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    val userState by userViewModel.userState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()

    Scaffold(
        containerColor = ColorBg,
        topBar = {
            SettingsTopBar(onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 0. User / Identity Section
            UserIdentityHeader(
                userState = userState,
                onNavigateToAuth = onNavigateToAuth,
                onLogout = { userViewModel.logout(onLogout) }
            )

            // 1. AGENDA
            SettingsSection(title = "AGENDA", icon = Icons.Default.CalendarToday) {
                ToggleRow(
                    label = "Recordatorios de actividad",
                    description = "Avisar antes de empezar",
                    checked = settingsState.remindersEnabled,
                    onCheckedChange = settingsViewModel::toggleReminders
                )
                ActionRow(label = "Tiempo de aviso", value = "5 min antes") { /* Future impl */ }
                ActionRow(label = "Actividades flexibles", value = "Mostrar siempre") { /* Future impl */ }
            }

            // 2. ESTADÍSTICAS
            SettingsSection(title = "ESTADÍSTICAS", icon = Icons.Default.BarChart) {
                ToggleRow(
                    label = "Mostrar mapa de calor",
                    description = "Visualizar aportaciones diarias",
                    checked = settingsState.showHeatmap,
                    onCheckedChange = settingsViewModel::toggleHeatmap
                )
                ToggleRow(
                    label = "Insights automáticos",
                    description = "Consejos basados en historial",
                    checked = settingsState.showInsights,
                    onCheckedChange = settingsViewModel::toggleInsights
                )
                ActionRow(label = "Período de análisis", value = "90 días") { /* Future impl */ }
            }

            // 3. DATOS
            SettingsSection(title = "DATOS Y PRIVACIDAD", icon = Icons.Default.Storage) {
                ActionRow(
                    label = "Copia de seguridad local",
                    description = "Exportar base de datos a archivo",
                    onClick = onNavigateToBackup
                )
                ActionRow(label = "Información de almacenamiento", value = "1.2 MB") { /* Future impl */ }
            }

            // 4. SISTEMA
            SettingsSection(title = "SISTEMA", icon = Icons.Default.Settings) {
                InfoRow(label = "Versión", value = "1.0.2 (Public Beta)")
                InfoRow(label = "Base de Datos", value = "RoutineOS_Core_v9")
                ActionRow(label = "Herramientas de diagnóstico") { /* Future impl */ }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun UserIdentityHeader(
    userState: UserState,
    onNavigateToAuth: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        color = ColorSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        when (userState) {
            is UserState.Success -> {
                val user = userState.user
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(ColorBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = ColorTextDim)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, style = TitleNode.copy(fontSize = 15.sp))
                        Text(user.email, style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, null, tint = ColorPending, modifier = Modifier.size(20.dp))
                    }
                }
            }
            else -> {
                Row(
                    modifier = Modifier.padding(16.dp).clickable { onNavigateToAuth() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).background(ColorBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountCircle, null, tint = ColorTextDim)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Sincronización desactivada", style = TitleNode.copy(fontSize = 15.sp))
                        Text("Toca para iniciar sesión", style = MetaMono.copy(fontSize = 9.sp), color = ColorExec)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = ColorTextMuted)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MetaMono.copy(fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
                color = ColorTextMuted
            )
        }
        Surface(
            color = ColorSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = TitleNode.copy(fontSize = 13.sp))
            if (description != null) {
                Text(description, style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ColorExec,
                uncheckedThumbColor = ColorTextDim,
                uncheckedTrackColor = ColorBg
            )
        )
    }
}

@Composable
private fun ActionRow(
    label: String,
    value: String? = null,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = TitleNode.copy(fontSize = 13.sp))
            if (description != null) {
                Text(description, style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
            }
        }
        if (value != null) {
            Text(value, style = MetaMono.copy(fontSize = 10.sp), color = ColorTextMuted)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(14.dp), tint = ColorTextMuted)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = TitleNode.copy(fontSize = 13.sp), modifier = Modifier.weight(1f))
        Text(value, style = MetaMono.copy(fontSize = 10.sp), color = ColorTextMuted)
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ColorTextDim, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "CONFIGURACIÓN",
            style = MetaMono.copy(fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
            color = ColorText
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(32.dp))
    }
}
