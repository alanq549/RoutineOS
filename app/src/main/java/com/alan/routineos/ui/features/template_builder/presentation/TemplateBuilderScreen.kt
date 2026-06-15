package com.alan.routineos.ui.features.template_builder.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.util.ScheduleResolver
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.data.local.entities.TemporalMode
import com.alan.routineos.ui.features.template_builder.components.NodeScheduleSheet
import com.alan.routineos.ui.features.template_builder.sections.ActivityIdentitySection
import com.alan.routineos.ui.features.template_builder.sections.AdvancedSection
import com.alan.routineos.ui.features.template_builder.sections.ColorSection
import com.alan.routineos.ui.features.template_builder.sections.RepeatSection
import com.alan.routineos.ui.features.template_builder.sections.TimeRangeSection
import com.alan.routineos.ui.features.template_builder.sections.nodeStructureSection
import com.alan.routineos.ui.features.template_builder.viewmodel.TemplateBuilderViewModel
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorText
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.ColorTextMuted
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateBuilderScreen(
    onBack: () -> Unit,
    onNavigateToTypeManager: () -> Unit,
    viewModel: TemplateBuilderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNodeForSchedule by remember { mutableStateOf<Node?>(null) }
    var showStructure by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val colors = listOf(
        Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFC107),
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0),
        Color(0xFF795548), Color(0xFF607D8B)
    )

    val canSave = uiState.name.isNotBlank() && when (uiState.temporalMode) {
        TemporalMode.NONE -> true
        TemporalMode.SEQUENTIAL -> uiState.durationMinutes > 0
        TemporalMode.START_ONLY -> uiState.startTime.isNotBlank() && uiState.startTime != "--:--"
        TemporalMode.START_END -> uiState.startTime.isNotBlank() &&
                uiState.startTime != "--:--" &&
                uiState.endTime.isNotBlank() &&
                uiState.endTime != "--:--"
    }

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
                    onClick = { viewModel.saveTemplate(onSuccess = onBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "GUARDAR Y SALIR",
                        style = TitleNode.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.Red.copy(alpha = 0.3f)
                    ),
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
            TemplateBuilderTopBar(
                isNewTemplate = uiState.templateId == null,
                canSave = canSave,
                hasChanges = uiState.hasUnsavedChanges,
                onBack = { viewModel.handleBackPress(onBack) },
                onSave = {
                    if (canSave) {
                        viewModel.saveTemplate(onSuccess = onBack)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ColorBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                ActivityIdentitySection(
                    name = uiState.name,
                    onNameChange = viewModel::updateName
                )
            }

            item {
                AdvancedSection(title = "APARIENCIA") {
                    Column(modifier = Modifier.padding(bottom = 24.dp)) {
                        ColorSection(
                            selectedColorHex = uiState.colorHex,
                            colors = colors,
                            onColorChange = viewModel::updateColor
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }

            item {
                TimeRangeSection(
                    temporalMode = uiState.temporalMode,
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    durationMinutes = uiState.durationMinutes,
                    hasNodeSchedules = uiState.nodeSchedules.any { it.value.isNotEmpty() },
                    onTemporalModeChange = viewModel::updateTemporalMode,
                    onStartTimeChange = viewModel::updateStartTime,
                    onEndTimeChange = viewModel::updateEndTime,
                    onDurationChange = viewModel::updateDurationMinutes
                )
            }

            item {
                RepeatSection(
                    selectedDays = uiState.selectedDays,
                    onToggleDay = viewModel::toggleDay
                )
            }

            if (!showStructure && uiState.nodes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { showStructure = true }
                            .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Add,
                                null,
                                tint = ColorExec,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "DEFINIR ESTRUCTURA INTERNA",
                                style = MetaMono.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ColorText
                            )
                            Text(
                                "Divide esta rutina en bloques o tareas",
                                style = MetaMono.copy(fontSize = 9.sp),
                                color = ColorTextDim
                            )
                        }
                    }
                }
            } else {
                nodeStructureSection(
                    nodes = uiState.nodes,
                    nodeSchedules = uiState.nodeSchedules,
                    fieldValues = uiState.fieldValues,
                    nodeTypes = uiState.nodeTypes,
                    metadataSchemas = uiState.metadataSchemas,
                    activityName = uiState.name,
                    activityDays = uiState.selectedDays,
                    activityStart = uiState.startTime,
                    activityEnd = uiState.endTime,
                    onAddNode = { parentId -> viewModel.addNode("", "default", parentId) },
                    onUpdateNodeName = viewModel::updateNodeName,
                    onUpdateNodeType = viewModel::updateNodeType,
                    onUpdateFieldValue = viewModel::updateFieldValue,
                    onDeleteNode = viewModel::deleteNode,
                    onScheduleClick = { selectedNodeForSchedule = it },
                    onManageDetailsClick = onNavigateToTypeManager
                )
            }
        }

        if (selectedNodeForSchedule != null) {
            val selectedNode = selectedNodeForSchedule!!
            val ownSchedules = uiState.nodeSchedules[selectedNode.id].orEmpty()
            val inheritanceInfo = ScheduleResolver.resolveInheritanceInfo(
                nodeId = selectedNode.id,
                allNodes = uiState.nodes,
                nodeSchedules = uiState.nodeSchedules,
                activityName = uiState.name,
                activityDays = uiState.selectedDays,
                activityStart = uiState.startTime,
                activityEnd = uiState.endTime
            )

            val inheritedSchedules = inheritanceInfo?.second ?: emptyList()
            val schedulesForSheet =
                if (ownSchedules.isNotEmpty()) ownSchedules else inheritedSchedules
            val isUsingInheritedSchedule = ownSchedules.isEmpty() && inheritedSchedules.isNotEmpty()

            NodeScheduleSheet(
                node = selectedNode,
                currentSchedules = schedulesForSheet,
                isUsingInheritedSchedule = isUsingInheritedSchedule,
                inheritedSchedules = inheritedSchedules,
                inheritedSource = inheritanceInfo?.first,
                onDismiss = { selectedNodeForSchedule = null },
                onToggleSequential = { isSequential ->
                    viewModel.toggleNodeSequential(selectedNode.id, isSequential)
                },
                onSave = { schedules ->
                    viewModel.updateNodeSchedules(selectedNode.id, schedules)
                    selectedNodeForSchedule = null
                }
            )
        }
    }
}

@Composable
private fun TemplateBuilderTopBar(
    isNewTemplate: Boolean,
    canSave: Boolean,
    hasChanges: Boolean = false,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(36.dp)
                .background(ColorSurface, CircleShape)
                .border(0.5.dp, ColorBorder, CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar",
                tint = ColorTextDim,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = if (isNewTemplate) "NUEVA ACTIVIDAD" else "EDITAR ACTIVIDAD",
            style = MetaMono.copy(
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = ColorText
        )

        Button(
            onClick = onSave,
            enabled = canSave,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasChanges) ColorExec else ColorSurface,
                contentColor = if (hasChanges) Color.White else ColorExec,
                disabledContainerColor = ColorSurface.copy(alpha = 0.5f),
                disabledContentColor = ColorTextMuted
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            modifier = Modifier.height(36.dp),
            border = if (!hasChanges && canSave) androidx.compose.foundation.BorderStroke(
                1.dp,
                ColorExec.copy(alpha = 0.5f)
            ) else null
        ) {
            Text(
                text = "GUARDAR",
                style = TitleNode.copy(fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}
