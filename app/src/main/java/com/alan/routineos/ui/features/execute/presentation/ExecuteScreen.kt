package com.alan.routineos.ui.features.execute.presentation

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.execute.components.DynamicField
import com.alan.routineos.ui.features.execute.components.SessionHistoryCard
import com.alan.routineos.ui.features.execute.viewmodel.ExecuteViewModel
import com.alan.routineos.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecuteScreen(
    onBack: () -> Unit,
    viewModel: ExecuteViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    BackHandler {
        viewModel.handleBackPress(onBack)
    }

    if (uiState.showExitConfirmation) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setShowExitConfirmation(false) },
            containerColor = ColorSurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = ColorBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tienes cambios sin guardar",
                    style = TitleNode.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = ColorText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Si sales ahora, perderás los cambios que no hayas guardado.",
                    style = MetaMono.copy(fontSize = 12.sp),
                    color = ColorTextDim,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.saveChanges(onComplete = onBack) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("GUARDAR Y SALIR", style = TitleNode.copy(color = Color.White))
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("DESCARTAR CAMBIOS", style = TitleNode)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { viewModel.setShowExitConfirmation(false) }) {
                    Text("SEGUIR EDITANDO", color = ColorTextDim, style = TitleNode)
                }
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
                    IconButton(onClick = { viewModel.handleBackPress(onBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveChanges() }) {
                        Text("GUARDAR", color = ColorExec, style = TitleNode)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBg,
                    navigationIconContentColor = ColorText
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorBg
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorExec)
            }
        } else {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)) {
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

                    Spacer(modifier = Modifier.height(32.dp))

                    uiState.schemas.forEach { schema ->
                        val isInstanceNode = uiState.node?.instanceId != null
                        val isReadonly = if (isInstanceNode) {
                            !schema.editableInExecution
                        } else {
                            !schema.editableInTemplate
                        }

                        DynamicField(
                            schema = schema,
                            currentValue = uiState.fieldValues[schema.fieldName] ?: "",
                            readonly = isReadonly,
                            onValueChange = { newValue ->
                                viewModel.updateDraftValue(schema.fieldName, newValue)
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    if (uiState.history.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("ÚLTIMOS VALORES REGISTRADOS", style = MetaMono, color = ColorTextMuted)
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
                            viewModel.saveAndComplete()
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "FINALIZAR ACTIVIDAD",
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
