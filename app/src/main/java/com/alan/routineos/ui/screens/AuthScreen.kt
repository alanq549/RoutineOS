package com.alan.routineos.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.remote.auth.device.DeviceRequest
import com.alan.routineos.data.remote.auth.login.LoginRequest
import com.alan.routineos.data.remote.auth.register.RegisterRequest
import com.alan.routineos.ui.state.VerifyEmailState
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.GlassWhite
import com.alan.routineos.ui.theme.NeonEmerald
import com.alan.routineos.ui.theme.TextSecondary
import com.alan.routineos.ui.viewmodel.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.alan.routineos.data.remote.auth.DevicePlatform
import com.alan.routineos.ui.components.RoutineTextField
import com.alan.routineos.ui.state.AuthState
import com.alan.routineos.ui.state.AuthStep


import android.os.Build

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
) {

    var step by remember { mutableStateOf(AuthStep.LOGIN) }

    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

    val platform = remember {
        DevicePlatform.ANDROID // Android-only build (Compose Android)
    }

    // LOGIN
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // REGISTER
    var name by remember { mutableStateOf("") }
    var registerEmail by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }

    // VERIFY
    var code by remember { mutableStateOf("") }
    var pendingEmail by remember { mutableStateOf("") }

    val verifyState by viewModel.verifyState.collectAsState()

    // 🔥 Solo reacción UI a éxito de verificación
    LaunchedEffect(verifyState) {
        if (verifyState is VerifyEmailState.Success) {
            step = AuthStep.LOGIN
            code = ""
            pendingEmail = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(24.dp)
    ) {

        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-150).dp, y = (-150).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            NeonEmerald.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "RoutineOS",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = when (step) {
                    AuthStep.LOGIN -> "SYSTEM ACCESS // BETA"
                    AuthStep.REGISTER -> "CREATE ACCOUNT // BETA"
                    AuthStep.VERIFY -> "VERIFY EMAIL // BETA"
                },
                fontSize = 10.sp,
                color = NeonEmerald,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .background(GlassWhite)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                    label = "auth_flow"
                ) { currentStep ->

                    when (currentStep) {

                        // ================= LOGIN =================
                        AuthStep.LOGIN -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                RoutineTextField(
                                    value = loginEmail,
                                    onValueChange = { loginEmail = it },
                                    label = "Email",
                                    placeholder = "user@routineos.com"
                                )

                                RoutineTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = "Password",
                                    placeholder = "••••••••",
                                    isPassword = true
                                )

                                Button(
                                    onClick = {
                                        viewModel.login(
                                            LoginRequest(
                                                email = loginEmail,
                                                password = loginPassword,
                                                device = DeviceRequest(
                                                    platform = platform,
                                                    deviceName = deviceName,
                                                    deviceFingerprint = "routine_auth_fprint_001"
                                                )
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonEmerald
                                    )
                                ) {
                                    Text("INICIAR SESIÓN", color = BgDark)
                                }
                            }
                        }

                        // ================= REGISTER =================
                        AuthStep.REGISTER -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                RoutineTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = "Name",
                                    placeholder = "Tu nombre"
                                )

                                RoutineTextField(
                                    value = registerEmail,
                                    onValueChange = { registerEmail = it },
                                    label = "Email",
                                    placeholder = "user@routineos.com"
                                )

                                RoutineTextField(
                                    value = registerPassword,
                                    onValueChange = { registerPassword = it },
                                    label = "Password",
                                    placeholder = "••••••••",
                                    isPassword = true
                                )

                                Button(
                                    onClick = {
                                        val request = RegisterRequest(
                                            email = registerEmail,
                                            password = registerPassword,
                                            name = name,
                                            timezone = null,
                                            device = DeviceRequest(
                                                platform = platform,
                                                deviceName = deviceName,
                                                deviceFingerprint = "routine_auth_fprint_001"
                                            )
                                        )

                                        viewModel.register(
                                            request = request,
                                            onCodeSent = {
                                                pendingEmail = registerEmail
                                                step = AuthStep.VERIFY
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonEmerald
                                    )
                                ) {
                                    Text("CREAR CUENTA", color = BgDark)
                                }
                            }
                        }

                        // ================= VERIFY =================
                        AuthStep.VERIFY -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                Text("We sent a code to:", color = TextSecondary)
                                Text(pendingEmail, color = Color.White)

                                RoutineTextField(
                                    value = code,
                                    onValueChange = { code = it },
                                    label = "Code",
                                    placeholder = "123456"
                                )

                                Button(
                                    onClick = {
                                        viewModel.verifyEmailCode(code)
                                    },
                                    enabled = verifyState !is VerifyEmailState.Loading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonEmerald
                                    )
                                ) {
                                    if (verifyState is VerifyEmailState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = BgDark
                                        )
                                    } else {
                                        Text("VERIFY", color = BgDark)
                                    }
                                }

                                if (verifyState is VerifyEmailState.Error) {
                                    Text(
                                        text = (verifyState as VerifyEmailState.Error).message,
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (step != AuthStep.VERIFY) {
                Text(
                    text = if (step == AuthStep.LOGIN)
                        "¿No tienes cuenta? Regístrate"
                    else
                        "¿Ya tienes cuenta? Inicia sesión",
                    color = TextSecondary,
                    modifier = Modifier.clickable {
                        step =
                            if (step == AuthStep.LOGIN)
                                AuthStep.REGISTER
                            else
                                AuthStep.LOGIN
                    }
                )
            }
        }
    }
}