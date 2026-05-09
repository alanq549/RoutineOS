package com.alan.routineos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.data.local.entities.FieldType
import com.alan.routineos.data.local.entities.NodeFieldValue
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.ExecuteViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecuteScreen(
    onBack: () -> Unit,
    viewModel: ExecuteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    
    var timerRunning by remember { mutableStateOf(false) }
    var timeLeftSeconds by remember { mutableIntStateOf(0) }

    // Control del Timer
    LaunchedEffect(timerRunning, timeLeftSeconds) {
        if (timerRunning && timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds--
            if (timeLeftSeconds == 0) {
                timerRunning = false
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    // Escuchar señal del ViewModel para iniciar timer tras guardar set
    LaunchedEffect(uiState.shouldStartTimer) {
        uiState.shouldStartTimer?.let { minutes ->
            timeLeftSeconds = minutes * 60
            timerRunning = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.parentNode?.name ?: "Actividad", 
                        style = TitleNode,
                        color = ColorTextDim
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBg,
                    navigationIconContentColor = ColorText
                )
            )
        },
        containerColor = ColorBg
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorExec)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = uiState.node?.name ?: "",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.W500
                        ),
                        color = ColorText
                    )
                    
                    if (timerRunning) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d:%02d", timeLeftSeconds / 60, timeLeftSeconds % 60),
                            style = MonoTimer,
                            color = ColorExec,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    uiState.schemas.forEach { schema ->
                        DynamicField(
                            schema = schema,
                            currentValue = uiState.draftValues[schema.fieldName] ?: "",
                            onValueChange = { newValue ->
                                viewModel.updateDraftValue(schema.fieldName, newValue)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (uiState.history.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("HISTORIAL DE LA SESIÓN", style = MetaMono, color = ColorTextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.history.forEach { session ->
                            SessionHistoryCard(session)
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.saveIteration()
                            // Si no hay series pendientes o timer, podemos salir, 
                            // pero el ViewModel maneja la completitud.
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val seriesField = uiState.schemas.find { it.fieldName.lowercase().contains("ser") }
                        val isLast = (uiState.draftValues[seriesField?.fieldName]?.toIntOrNull() ?: 1) <= 1
                        
                        Text(
                            if (seriesField != null && !isLast) "GUARDAR SET" else "GUARDAR Y FINALIZAR", 
                            style = TitleNode.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicField(
    schema: NodeMetadataSchema,
    currentValue: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = schema.fieldLabel.uppercase(),
            style = MetaMono,
            color = ColorTextDim
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        when (schema.fieldType) {
            FieldType.NUMBER -> {
                NumericPicker(
                    value = currentValue,
                    unit = schema.unit,
                    stepSize = schema.stepSize ?: 1f,
                    onValueChange = onValueChange
                )
            }
            FieldType.DURATION -> {
                NumericPicker(
                    value = currentValue,
                    unit = "min",
                    stepSize = 1f,
                    onValueChange = onValueChange
                )
            }
            FieldType.BOOLEAN -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (currentValue.toBoolean()) "SÍ" else "NO", style = TitleNode, color = ColorText)
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = currentValue.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColorExec)
                    )
                }
            }
            else -> {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TitleNode,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ColorText,
                        unfocusedTextColor = ColorText,
                        focusedBorderColor = ColorExec,
                        unfocusedBorderColor = ColorBorder
                    )
                )
            }
        }
    }
}

@Composable
fun NumericPicker(
    value: String,
    unit: String?,
    stepSize: Float,
    onValueChange: (String) -> Unit
) {
    val numericValue = value.toFloatOrNull() ?: 0f
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            onClick = { onValueChange((numericValue - stepSize).toString()) },
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            color = ColorSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("-", color = ColorText, fontSize = 24.sp)
            }
        }
        
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MonoTimer.copy(fontSize = 32.sp),
                color = ColorText
            )
            if (unit != null) {
                Text(text = unit, style = MetaMono, color = ColorTextDim)
            }
        }
        
        Surface(
            onClick = { onValueChange((numericValue + stepSize).toString()) },
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            color = ColorSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorExec)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("+", color = ColorExec, fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun SessionHistoryCard(session: com.alan.routineos.ui.viewmodel.HistorySession) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface2)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            session.values.forEach { valItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(valItem.fieldName, style = MetaMono, color = ColorTextDim)
                    Text(valItem.value, style = TitleNode, color = ColorText)
                }
            }
        }
    }
}
