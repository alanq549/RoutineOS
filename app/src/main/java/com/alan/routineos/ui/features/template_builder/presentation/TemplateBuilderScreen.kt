package com.alan.routineos.ui.features.template_builder.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.ui.features.template_builder.components.NodeScheduleSheet
import com.alan.routineos.ui.features.template_builder.sections.*
import com.alan.routineos.ui.features.template_builder.viewmodel.TemplateBuilderViewModel
import com.alan.routineos.ui.theme.TitleNode
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.core.util.ScheduleResolver

@Composable
fun TemplateBuilderScreen(
    onBack: () -> Unit,
    onNavigateToTypeManager: () -> Unit,
    viewModel: TemplateBuilderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNodeForSchedule by remember { mutableStateOf<Node?>(null) }
    var showStructure by remember { mutableStateOf(false) } 

    val colors = listOf(
        Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFC107),
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0),
        Color(0xFF795548), Color(0xFF607D8B)
    )

    Scaffold(
        topBar = {
            TemplateBuilderTopBar(
                isNewTemplate = uiState.templateId == null,
                canSave = uiState.name.isNotBlank(),
                onBack = onBack,
                onSave = {
                    viewModel.saveTemplate(onSuccess = onBack)
                }
            )
        },
        containerColor = ColorBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 1. IDENTITY: ¿Qué vamos a organizar?
            item {
                ActivityIdentitySection(
                    name = uiState.name,
                    onNameChange = viewModel::updateName,
                    selectedCategory = uiState.category,
                    onCategoryChange = viewModel::updateCategory
                )
            }

            // 2. QUICK START (Presets)
            if (uiState.templateId == null && uiState.name.isBlank()) {
                item {
                    QuickPresetsSection(onSelect = { preset ->
                        viewModel.updateName(preset.name)
                        viewModel.updateColor(preset.colorHex)
                        viewModel.updateCategory(preset.category)
                    })
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 3. TEMPORAL: ¿CUÁNDO OCURRE?
            item {
                TimeRangeSection(
                    selectedMode = uiState.timeMode,
                    startTime = uiState.startTime,
                    endTime = uiState.endTime,
                    durationMinutes = uiState.durationMinutes,
                    onModeChange = viewModel::updateTimeMode,
                    onStartTimeChange = viewModel::updateStartTime,
                    onEndTimeChange = viewModel::updateEndTime,
                    onDurationChange = viewModel::updateDurationMinutes
                )
            }

            // 4. RECURRENCE: ¿QUÉ DÍAS SE REPITE?
            item {
                RepeatSection(
                    selectedDays = uiState.selectedDays,
                    onToggleDay = viewModel::toggleDay
                )
            }

            // 5. ESTRUCTURA INTERNA (PASOS)
            if (!showStructure && uiState.nodes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { showStructure = true }
                            .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Agregar pasos a esta actividad",
                            style = TitleNode.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                            color = ColorExec
                        )
                    }
                }
            } else {
                nodeStructureSection(
                    nodes = uiState.nodes,
                    nodeSchedules = uiState.nodeSchedules,
                    fieldValues = uiState.fieldValues,
                    nodeTypes = uiState.nodeTypes,
                    metadataSchemas = uiState.metadataSchemas,
                    onAddNode = { parentId -> 
                        viewModel.addNode("", "default", parentId) 
                    },
                    onUpdateNodeName = viewModel::updateNodeName,
                    onUpdateNodeType = viewModel::updateNodeType,
                    onUpdateFieldValue = viewModel::updateFieldValue,
                    onDeleteNode = viewModel::deleteNode,
                    onScheduleClick = { selectedNodeForSchedule = it },
                    onManageDetailsClick = onNavigateToTypeManager
                )
            }

            // 6. APARIENCIA
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
            
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        if (selectedNodeForSchedule != null) {
            val selectedNode = selectedNodeForSchedule!!
            val ownSchedules = uiState.nodeSchedules[selectedNode.id].orEmpty()
            val inheritedSchedules = ScheduleResolver.resolveInheritedSchedules(
                node = selectedNode,
                allNodes = uiState.nodes,
                nodeSchedules = uiState.nodeSchedules
            )

            val schedulesForSheet = if (ownSchedules.isNotEmpty()) ownSchedules else inheritedSchedules
            val isUsingInheritedSchedule = ownSchedules.isEmpty() && inheritedSchedules.isNotEmpty()

            NodeScheduleSheet(
                node = selectedNode,
                currentSchedules = schedulesForSheet,
                isUsingInheritedSchedule = isUsingInheritedSchedule,
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(ColorSurface, CircleShape)
                .border(0.5.dp, Color(0xFF2A2A2A), CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color(0xFF777777),
                modifier = Modifier.size(16.dp)
            )
        }
        
        Text(
            text = if (isNewTemplate) "NUEVA ACTIVIDAD" else "EDITAR ACTIVIDAD",
            style = TitleNode.copy(fontSize = 11.sp, letterSpacing = 1.2.sp),
            color = Color.White
        )
        
        Surface(
            onClick = onSave,
            color = if (canSave) ColorExec else Color(0xFF1A1A1A),
            shape = RoundedCornerShape(6.dp),
            enabled = canSave
        ) {
            Text(
                "LISTO",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = TitleNode.copy(fontSize = 10.sp),
                color = if (canSave) Color.White else Color(0xFF444444)
            )
        }
    }
}
