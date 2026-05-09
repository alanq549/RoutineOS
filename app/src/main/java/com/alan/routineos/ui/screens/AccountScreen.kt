package com.alan.routineos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.state.UserState
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val userState by userViewModel.userState.collectAsState()

    Scaffold(
        containerColor = BgDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "PERFIL // SISTEMA",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                        fontSize = 14.sp,
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = NeonEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BgDark.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
            // Glow background similar to Auth
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 100.dp, y = (-50).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonEmerald.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
            )

            when (val state = userState) {
                is UserState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonEmerald)
                    }
                }

                is UserState.Success -> {
                    val user = state.user
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(GlassWhite)
                                .border(1.dp, NeonEmerald.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = NeonEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user.name.uppercase(),
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = JetBrainsMono
                        )
                        Text(
                            text = "ESTADO: ACTIVO",
                            color = NeonEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // Info Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(GlassWhite)
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            InfoItem(label = "NOMBRE COMPLETO", value = user.name)
                            InfoItem(label = "IDENTIFICACIÓN", value = user.id)
                            InfoItem(label = "CORREO ELECTRÓNICO", value = user.email)
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Logout Button
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("CERRAR SESIÓN", color = Color.Red, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono, fontSize = 12.sp)
                        }
                    }
                }
                else -> { /* Error handled in state */ }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 9.sp,
            fontFamily = JetBrainsMono,
            color = NeonEmerald.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
