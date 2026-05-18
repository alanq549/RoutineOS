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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.account.state.UserState
import com.alan.routineos.ui.features.account.viewmodel.UserViewModel
import com.alan.routineos.ui.theme.*

/**
 * ACCOUNT SCREEN: Premium Minimal / Calm Tech
 * Integrated with the personal operating system identity.
 */
@Composable
fun AccountScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val userState by userViewModel.userState.collectAsState()

    Scaffold(
        containerColor = ColorBg,
        topBar = {
            AccountTopBar(onBack)
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = userState) {
                is UserState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ColorExec)
                    }
                }

                is UserState.Success -> {
                    val user = state.user
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Identity Section
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(ColorSurface, CircleShape)
                                .border(0.5.dp, ColorBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = ColorTextDim
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = ColorText
                        )
                        
                        Surface(
                            color = ColorExec.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Sincronización activa",
                                style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = ColorExec,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // Settings Groups
                        InfoSection(title = "Detalles de la cuenta") {
                            InfoRow(label = "Correo", value = user.email)
                            InfoRow(label = "ID de Sistema", value = user.id, isMono = true)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Area
                        Button(
                            onClick = {
                                // Llamamos al proceso de logout y pasamos la navegación como callback
                                userViewModel.logout(onComplete = onLogout)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Red.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "Cerrar sesión",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.Red,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
                else -> {
                    // Idle state or Login Prompt
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = ColorTextMuted
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Sin sesión activa",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = ColorText
                        )
                        Text(
                            text = "Inicia sesión para sincronizar tus datos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ColorTextDim,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = onNavigateToAuth,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountTopBar(onBack: () -> Unit) {
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
            text = "CUENTA",
            style = MetaMono.copy(fontSize = 11.sp, letterSpacing = 2.sp),
            color = ColorTextDim
        )
        
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextMuted,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Surface(
            color = ColorSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ColorTextDim
        )
        Text(
            text = value,
            style = if (isMono) MetaMono.copy(fontSize = 11.sp) else MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = ColorText
        )
    }
}
