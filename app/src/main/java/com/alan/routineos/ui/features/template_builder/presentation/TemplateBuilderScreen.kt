package com.alan.routineos.ui.features.template_builder.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.ui.features.template_builder.components.MetadataFieldItem
import com.alan.routineos.ui.features.template_builder.components.NodeScheduleSheet
import com.alan.routineos.ui.features.template_builder.components.SectionHeader
import com.alan.routineos.ui.features.template_builder.sections.*
import com.alan.routineos.ui.features.template_builder.viewmodel.TemplateBuilderViewModel
import com.alan.routineos.ui.theme.TitleNode

/**
 * UI REFACTOR: Template Builder Screen
 * Focused on a temporal-first workflow (Routine Composer).
 */
@Composable
fun TemplateBuilderScreen(
    onBack: () -> Unit,
    onNavigateToTypeManager: () -> Unit,
    viewModel: TemplateBuilderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedNodeForSchedule by remember { mutableStateOf<Node?>(null) }
    
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
                onSave = viewModel::saveTemplate
            )
        },
        containerColor = Color(0xFF0D1117)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // 1. IDENTITY: Name + Activity Type
            item {
                ActivityIdentitySection(
                    name = uiState.name,
                    onNameChange = viewModel::updateName
                )
            }

            // 2. TEMPORAL PLANNING: Time Mode Selector
            item {
                TimeRangeSection()
            }

            // 3. RECURRENCE: Weekly Pattern
            item {
                RepeatSection()
            }

            // 4. ROUTINE COMPOSITION: Internal Structure
            nodeStructureSection(
                nodes = uiState.nodes,
                nodeSchedules = uiState.nodeSchedules,
                onAddNode = { parentId -> 
                    viewModel.addNode("Nuevo Nodo", "default_type", parentId) 
                },
                onDeleteNode = viewModel::deleteNode,
                onScheduleClick = { selectedNodeForSchedule = it }
            )

            // 5. ADVANCED CONFIG: Collapsible Panel (Color + Metadata)
            item {
                AdvancedSection {
                    Column(modifier = Modifier.padding(bottom = 24.dp)) {
                        ColorSection(
                            selectedColorHex = uiState.colorHex,
                            colors = colors,
                            onColorChange = viewModel::updateColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        SectionHeader(title = "Campos personalizados", onAdd = onNavigateToTypeManager)
                        MetadataFieldItem(type = "NUM", name = "series", value = "def: 4")
                        MetadataFieldItem(type = "NUM", name = "reps", value = "def: 8")
                        MetadataFieldItem(type = "NUM", name = "peso", value = "unidad: kg")
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        if (selectedNodeForSchedule != null) {
            NodeScheduleSheet(
                node = selectedNodeForSchedule!!,
                currentSchedules = uiState.nodeSchedules[selectedNodeForSchedule!!.id] ?: emptyList(),
                onDismiss = { selectedNodeForSchedule = null },
                onToggleSequential = { isSequential ->
                    viewModel.toggleNodeSequential(selectedNodeForSchedule!!.id, isSequential)
                },
                onSave = { schedules ->
                    viewModel.updateNodeSchedules(selectedNodeForSchedule!!.id, schedules)
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
                .background(Color(0xFF1A1A1A), CircleShape)
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
            text = if (isNewTemplate) "COMPOSER" else "EDITOR",
            style = TitleNode.copy(fontSize = 12.sp, letterSpacing = 0.1.sp),
            color = Color(0xFFE0E0E0)
        )
        
        Surface(
            onClick = onSave,
            color = if (canSave) Color(0xFF1565C0) else Color(0xFF1A1A1A),
            shape = RoundedCornerShape(6.dp),
            enabled = canSave
        ) {
            Text(
                "SAVE",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                style = TitleNode.copy(fontSize = 11.sp),
                color = if (canSave) Color.White else Color(0xFF444444)
            )
        }
    }
}
