package com.alan.routineos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.alan.routineos.data.local.entities.NodeMetadataSchema
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.ColorTextMuted
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.MonoTimer
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.viewmodel.ExecuteViewModel
import com.alan.routineos.ui.viewmodel.HistorySession
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


    // Timer state for DURATION fields
    var timerRunning by remember { mutableStateOf(false) }
    var timeLeftSeconds by remember { mutableIntStateOf(0) }

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
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
                            text = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                timeLeftSeconds / 60,
                                timeLeftSeconds % 60
                            ),
                            style = MonoTimer,
                            color = ColorExec,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Dynamic form rendering
                    uiState.schemas.forEach { schema ->
                        DynamicField(
                            schema = schema,
                            currentValue = uiState.draftValues[schema.fieldName]
                                ?: schema.defaultValue ?: "",
                            onValueChange = { newValue ->
                                viewModel.updateDraftValue(schema.fieldName, newValue)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (uiState.history.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("HISTORIAL RECIENTE", style = MetaMono, color = ColorTextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.history.take(3).forEach { hist ->
                            HistoryRow(hist)
                        }
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }

                // Fixed bottom button
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

                            // Check for DURATION fields to start timer if hasMetricFields is true
                            if (uiState.nodeType?.hasMetricFields == true) {
                                val durationSchema =
                                    uiState.schemas.find { it.fieldType == FieldType.DURATION }
                                if (durationSchema != null) {
                                    val durationVal =
                                        uiState.draftValues[durationSchema.fieldName]?.toIntOrNull()
                                            ?: 0
                                    if (durationVal > 0) {
                                        timeLeftSeconds = durationVal * 60
                                        timerRunning = true
                                    }
                                }
                            }

                            viewModel.completeNode()
                            if (!timerRunning) onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Guardar y continuar",
                            style = TitleNode.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
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

            FieldType.TEXT -> {
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

            FieldType.BOOLEAN -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (currentValue.toBoolean()) "SÍ" else "NO",
                        style = TitleNode,
                        color = ColorText
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Switch(
                        checked = currentValue.toBoolean(),
                        onCheckedChange = { onValueChange(it.toString()) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColorExec)
                    )
                }
            }

            FieldType.DURATION -> {
                NumericPicker(
                    value = currentValue,
                    unit = "min",
                    stepSize = 1f,
                    onValueChange = onValueChange
                )
            }

            else -> {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
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
fun HistoryRow(session: HistorySession) {
    Column {
        session.values.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(it.fieldName, style = MetaMono, color = ColorTextDim)
                Text(it.value, style = TitleNode, color = ColorText)
            }
        }
    }
}
