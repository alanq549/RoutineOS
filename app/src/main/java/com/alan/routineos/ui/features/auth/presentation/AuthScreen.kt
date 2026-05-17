package com.alan.routineos.ui.features.auth.presentation

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.alan.routineos.ui.features.auth.state.AuthState
import com.alan.routineos.ui.features.auth.state.AuthStep
import com.alan.routineos.ui.features.auth.state.VerifyEmailState
import com.alan.routineos.ui.features.auth.viewmodel.AuthViewModel
import com.alan.routineos.ui.theme.*

/**
 * AUTH SCREEN: Premium Minimal / Calm Tech
 * Integrated with the personal operating system concept.
 * Focuses on clarity, privacy and human language.
 */
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

    val authError = (authState as? AuthState.Error)?.message
    val verifyError = (verifyState as? VerifyEmailState.Error)?.message

    val loginEnabled = loginEmail.isNotBlank() &&
            loginPassword.length >= 6 &&
            authState !is AuthState.Loading

    val registerEnabled = name.isNotBlank() &&
            registerEmail.isNotBlank() &&
            registerPassword.length >= 6

    val verifyEnabled = code.length == 6 &&
            code.all { it.isDigit() } &&
            verifyState !is VerifyEmailState.Loading

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.EmailNotVerified -> {
                pendingEmail = (authState as AuthState.EmailNotVerified).email
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

    Scaffold(
        containerColor = ColorBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Branding - Calm and integrated
            Text(
                text = "RoutineOS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = ColorText
            )
            
            Text(
                text = when (step) {
                    AuthStep.LOGIN -> "Inicia sesión para continuar"
                    AuthStep.REGISTER -> "Crea tu cuenta personal"
                    AuthStep.VERIFY -> "Verificación de seguridad"
                },
                style = MetaMono.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
                color = ColorTextDim
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Auth Container
            Surface(
                color = ColorSurface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(0.5.dp, ColorBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            fadeIn().togetherWith(fadeOut()).using(SizeTransform(clip = false))
                        },
                        label = "auth_flow"
                    ) { currentStep ->
                        when (currentStep) {
                            AuthStep.LOGIN -> {
                                LoginStep(
                                    email = loginEmail,
                                    onEmailChange = { loginEmail = it },
                                    password = loginPassword,
                                    onPasswordChange = { loginPassword = it },
                                    error = authError,
                                    isLoading = authState is AuthState.Loading,
                                    enabled = loginEnabled,
                                    onLogin = {
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
                                    }
                                )
                            }
                            AuthStep.REGISTER -> {
                                RegisterStep(
                                    name = name,
                                    onNameChange = { name = it },
                                    email = registerEmail,
                                    onEmailChange = { registerEmail = it },
                                    password = registerPassword,
                                    onPasswordChange = { registerPassword = it },
                                    error = authError,
                                    enabled = registerEnabled,
                                    onRegister = {
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
                                    }
                                )
                            }
                            AuthStep.VERIFY -> {
                                VerifyStep(
                                    email = pendingEmail,
                                    code = code,
                                    onCodeChange = { code = it },
                                    error = verifyError,
                                    isLoading = verifyState is VerifyEmailState.Loading,
                                    enabled = verifyEnabled,
                                    onVerify = { viewModel.verifyEmailCode(code) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (step != AuthStep.VERIFY) {
                Text(
                    text = if (step == AuthStep.LOGIN) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ColorTextDim,
                    modifier = Modifier.clickable {
                        step = if (step == AuthStep.LOGIN) AuthStep.REGISTER else AuthStep.LOGIN
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginStep(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?,
    isLoading: Boolean,
    enabled: Boolean,
    onLogin: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RoutineTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Correo electrónico",
            placeholder = "ej: usuario@routineos.com"
        )

        RoutineTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            placeholder = "••••••••",
            isPassword = true
        )

        error?.let { RoutineErrorCard(message = it) }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLogin,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = ColorBg,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Iniciar sesión", 
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun RegisterStep(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    onRegister: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RoutineTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Nombre",
            placeholder = "Tu nombre completo"
        )

        RoutineTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Correo electrónico",
            placeholder = "ej: usuario@routineos.com"
        )

        RoutineTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            placeholder = "Mínimo 6 caracteres",
            isPassword = true
        )

        error?.let { RoutineErrorCard(message = it) }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRegister,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Crear cuenta", 
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun VerifyStep(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    error: String?,
    isLoading: Boolean,
    enabled: Boolean,
    onVerify: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(text = "Enviamos un código a:", style = MetaMono, color = ColorTextDim)
            Text(
                text = email, 
                style = MaterialTheme.typography.bodyLarge, 
                color = ColorText, 
                fontWeight = FontWeight.Medium
            )
        }

        RoutineTextField(
            value = code,
            onValueChange = onCodeChange,
            label = "Código de verificación",
            placeholder = "123456"
        )

        error?.let { RoutineErrorCard(message = it) }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onVerify,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = ColorBg,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Verificar cuenta", 
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
