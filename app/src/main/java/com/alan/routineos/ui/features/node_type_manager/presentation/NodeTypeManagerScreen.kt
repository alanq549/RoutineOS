package com.alan.routineos.ui.features.node_type_manager.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.alan.routineos.ui.theme.*

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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp).background(ColorSurface, CircleShape)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = ColorTextDim, modifier = Modifier.size(16.dp))
                }

                Text(
                    text = "MOLDES DE INFORMACIÓN",
                    style = TitleNode.copy(fontSize = 11.sp, letterSpacing = 1.sp),
                    color = Color.White
                )

                Text(
                    "CREAR",
                    modifier = Modifier.clickable { showCreateTypeSheet = true },
                    style = TitleNode.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = ColorExec
                )
            }
        },
        containerColor = ColorBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, null, tint = ColorExec, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MIS HERRAMIENTAS", style = MetaMono.copy(fontSize = 9.sp), color = ColorTextDim)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Crea 'moldes' para tus actividades. Por ejemplo, un molde de 'Gym' que siempre pida peso y repeticiones, o uno de 'Uni' para el número de aula.",
                        style = TitleNode.copy(fontSize = 12.sp, lineHeight = 18.sp),
                        color = ColorTextDim.copy(alpha = 0.6f)
                    )
                }

                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.nodeTypes.forEach { type ->
                        val isSelected = uiState.selectedType?.id == type.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectType(if (isSelected) null else type) },
                            label = { Text(type.name, style = MetaMono.copy(fontSize = 10.sp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = ColorSurface,
                                selectedContainerColor = ColorExec.copy(alpha = 0.2f),
                                labelColor = ColorTextDim,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF222222),
                                selectedBorderColor = ColorExec
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.selectedType != null) {
                item {
                    Text(
                        "DATOS QUE PIDE EL MOLDE: ${uiState.selectedType!!.name.uppercase()}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MetaMono.copy(fontSize = 9.sp),
                        color = ColorExec
                    )
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
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ColorBorder)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp), tint = ColorExec)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar campo a este molde", style = TitleNode.copy(fontSize = 12.sp), color = Color.White)
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Text("Toca un molde arriba para ver sus detalles", style = MetaMono.copy(fontSize = 10.sp), color = Color(0xFF333333))
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
                onAdd = { name, label, type, default, unit ->
                    viewModel.addSchemaFull(uiState.selectedType!!.id, name, label, type, default, unit)
                    showAddSchemaSheet = false
                }
            )
        }
    }
}
