package com.alan.routineos.ui.features.node_type_manager.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.node_type_manager.components.schema.SchemaFieldItem
import com.alan.routineos.ui.features.node_type_manager.components.sheets.AddSchemaSheet
import com.alan.routineos.ui.features.node_type_manager.components.sheets.CreateTypeSheet
import com.alan.routineos.ui.features.node_type_manager.viewmodel.NodeTypeManagerViewModel
import com.alan.routineos.ui.theme.ColorBg
import com.alan.routineos.ui.theme.ColorBorder
import com.alan.routineos.ui.theme.ColorExec
import com.alan.routineos.ui.theme.ColorSurface
import com.alan.routineos.ui.theme.ColorTextDim
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NodeTypeManagerScreen(
    onBack: () -> Unit,
    viewModel: NodeTypeManagerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateTypeSheet by remember { mutableStateOf(false) }
    var showAddSchemaSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp
                    ), 
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(ColorSurface, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        tint = ColorTextDim,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "ESTRUCTURAS DE RUTINA",
                    style = TitleNode.copy(fontSize = 11.sp, letterSpacing = 1.2.sp),
                    color = Color.White
                )

                Text(
                    "NUEVA",
                    modifier = Modifier
                        .clickable { showCreateTypeSheet = true }
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                    style = TitleNode.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                    color = ColorExec
                )
            }
        },
        containerColor = ColorBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                Column(
                    modifier = Modifier.padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 12.dp,
                        bottom = 16.dp
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Build,
                            null,
                            tint = ColorExec.copy(alpha = 0.8f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "ARQUITECTURA DE HÁBITOS",
                            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                            color = ColorTextDim
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Diseña plantillas inteligentes para tus actividades diarias. Configura una estructura de 'Gimnasio' que registre series y peso, o una de 'Estudio' que guarde el número de aula de forma automática.",
                        style = TitleNode.copy(fontSize = 12.sp, lineHeight = 19.sp),
                        color = ColorTextDim.copy(alpha = 0.7f)
                    )
                }

                FlowRow(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.nodeTypes.forEach { type ->
                        val isSelected = uiState.selectedType?.id == type.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectType(if (isSelected) null else type) },
                            label = {
                                Text(
                                    type.name.uppercase(),
                                    style = MetaMono.copy(fontSize = 10.sp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ColorSurface,
                                selectedContainerColor = ColorExec.copy(alpha = 0.15f),
                                labelColor = ColorTextDim,
                                selectedLabelColor = ColorExec
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF1F1F1F),
                                selectedBorderColor = ColorExec
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            if (uiState.selectedType != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "MÉTRICAS Y DATOS DE REGISTRO: ${uiState.selectedType!!.name.uppercase()}",
                            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                            color = ColorExec,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            "ARCHIVAR",
                            modifier = Modifier
                                .clickable { viewModel.deleteNodeType(uiState.selectedType!!) }
                                .padding(start = 12.dp),
                            style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = Color.Red.copy(alpha = 0.6f)
                        )
                    }
                }

                items(uiState.schemasForSelectedType) { schema ->
                    SchemaFieldItem(
                        schema = schema,
                        onDelete = { viewModel.deleteSchema(schema) }
                    )
                }

                item {
                    Button(
                        onClick = { showAddSchemaSheet = true },
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSurface),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = ColorExec
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Añadir parámetro de seguimiento",
                            style = TitleNode.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White
                        )
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Selecciona una plantilla para gestionar sus parámetros",
                            style = MetaMono.copy(fontSize = 10.sp),
                            color = ColorTextDim.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }

        if (showCreateTypeSheet) {
            CreateTypeSheet(
                onDismiss = { showCreateTypeSheet = false },
                onCreate = { name, hasMetrics ->
                    viewModel.createNodeType(name, hasMetrics)
                    showCreateTypeSheet = false
                }
            )
        }

        if (showAddSchemaSheet && uiState.selectedType != null) {
            AddSchemaSheet(
                onDismiss = { showAddSchemaSheet = false },
                onAdd = { name, label, type, default, unit, inTemplate, inExec, mode ->
                    viewModel.addSchemaFull(
                        uiState.selectedType!!.id,
                        name,
                        label,
                        type,
                        default,
                        unit,
                        inTemplate,
                        inExec,
                        mode
                    )
                    showAddSchemaSheet = false
                }
            )
        }
    }
}
