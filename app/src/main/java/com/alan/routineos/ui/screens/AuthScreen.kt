package com.alan.routineos.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
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
import com.alan.routineos.data.remote.auth.DevicePlatform
import com.alan.routineos.data.remote.auth.device.DeviceRequest
import com.alan.routineos.data.remote.auth.login.LoginRequest
import com.alan.routineos.data.remote.auth.register.RegisterRequest
import com.alan.routineos.ui.components.RoutineErrorCard
import com.alan.routineos.ui.components.RoutineTextField
import com.alan.routineos.ui.state.AuthState
import com.alan.routineos.ui.state.AuthStep
import com.alan.routineos.ui.state.VerifyEmailState
import com.alan.routineos.ui.theme.BgDark
import com.alan.routineos.ui.theme.GlassWhite
import com.alan.routineos.ui.theme.NeonEmerald
import com.alan.routineos.ui.theme.TextSecondary
import com.alan.routineos.ui.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {

    var step by remember { mutableStateOf(AuthStep.LOGIN) }

    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
    val platform = DevicePlatform.ANDROID

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
    val authState by viewModel.authState.collectAsState()

    val authError =
        (authState as? AuthState.Error)?.message

    val verifyError =
        (verifyState as? VerifyEmailState.Error)?.message

    val loginEnabled =
        loginEmail.isNotBlank() &&
                loginPassword.length >= 6 &&
                authState !is AuthState.Loading

    val registerEnabled =
        name.isNotBlank() &&
                registerEmail.isNotBlank() &&
                registerPassword.length >= 6

    val verifyEnabled =
        code.length == 6 &&
                code.all { it.isDigit() } &&
                verifyState !is VerifyEmailState.Loading

    LaunchedEffect(authState) {

        when (authState) {

            is AuthState.EmailNotVerified -> {

                pendingEmail =
                    (authState as AuthState.EmailNotVerified).email

                step = AuthStep.VERIFY
            }

            is AuthState.Authenticated -> {
                onLoginSuccess()
            }

            else -> Unit
        }
    }

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

        // Glow background
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
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "RoutineOS",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (step) {
                    AuthStep.LOGIN -> "Acceso seguro"
                    AuthStep.REGISTER -> "Crear cuenta"
                    AuthStep.VERIFY -> "Verificar correo"
                },
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 420.dp)
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

                    transitionSpec = {
                        fadeIn()
                            .togetherWith(fadeOut())
                            .using(SizeTransform(clip = false))
                    },

                    label = "auth_flow"
                ) { currentStep ->

                    when (currentStep) {

                        // LOGIN
                        AuthStep.LOGIN -> {

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                RoutineTextField(
                                    value = loginEmail,
                                    onValueChange = {
                                        loginEmail = it
                                    },
                                    label = "Email",
                                    placeholder = "user@routineos.com"
                                )

                                RoutineTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                    },
                                    label = "Password",
                                    placeholder = "••••••••",
                                    isPassword = true
                                )

                                authError?.let {
                                    RoutineErrorCard(message = it)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

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
                                    enabled = loginEnabled,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonEmerald
                                    )
                                ) {

                                    if (authState is AuthState.Loading) {

                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = BgDark,
                                            strokeWidth = 2.dp
                                        )

                                    } else {

                                        Text(
                                            "INICIAR SESIÓN",
                                            color = BgDark
                                        )
                                    }
                                }
                            }
                        }

                        // REGISTER
                        AuthStep.REGISTER -> {

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                RoutineTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                    },
                                    label = "Nombre",
                                    placeholder = "Tu nombre"
                                )

                                RoutineTextField(
                                    value = registerEmail,
                                    onValueChange = {
                                        registerEmail = it
                                    },
                                    label = "Email",
                                    placeholder = "user@routineos.com"
                                )

                                RoutineTextField(
                                    value = registerPassword,
                                    onValueChange = {
                                        registerPassword = it
                                    },
                                    label = "Password",
                                    placeholder = "••••••••",
                                    isPassword = true
                                )

                                authError?.let {
                                    RoutineErrorCard(message = it)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {

                                        viewModel.register(
                                            request = RegisterRequest(
                                                email = registerEmail,
                                                password = registerPassword,
                                                name = name,
                                                timezone = null,
                                                device = DeviceRequest(
                                                    platform = platform,
                                                    deviceName = deviceName,
                                                    deviceFingerprint = "routine_auth_fprint_001"
                                                )
                                            ),

                                            onCodeSent = {
                                                pendingEmail = registerEmail
                                                step = AuthStep.VERIFY
                                            }
                                        )
                                    },

                                    enabled = registerEnabled,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonEmerald
                                    )
                                ) {

                                    Text(
                                        "CREAR CUENTA",
                                        color = BgDark
                                    )
                                }
                            }
                        }

                        // VERIFY
                        AuthStep.VERIFY -> {

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {

                                Text(
                                    text = "Enviamos un código a:",
                                    color = TextSecondary
                                )

                                Text(
                                    text = pendingEmail,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )

                                RoutineTextField(
                                    value = code,
                                    onValueChange = {
                                        code = it
                                    },
                                    label = "Código",
                                    placeholder = "123456"
                                )

                                verifyError?.let {
                                    RoutineErrorCard(message = it)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        viewModel.verifyEmailCode(code)
                                    },

                                    enabled = verifyEnabled,

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
                                            color = BgDark,
                                            strokeWidth = 2.dp
                                        )

                                    } else {

                                        Text(
                                            "VERIFICAR",
                                            color = BgDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (step != AuthStep.VERIFY) {

                Text(
                    text =
                        if (step == AuthStep.LOGIN) {
                            "¿No tienes cuenta? Regístrate"
                        } else {
                            "¿Ya tienes cuenta? Inicia sesión"
                        },

                    color = TextSecondary,

                    modifier = Modifier.clickable {

                        step =
                            if (step == AuthStep.LOGIN) {
                                AuthStep.REGISTER
                            } else {
                                AuthStep.LOGIN
                            }
                    }
                )
            }
        }
    }
}