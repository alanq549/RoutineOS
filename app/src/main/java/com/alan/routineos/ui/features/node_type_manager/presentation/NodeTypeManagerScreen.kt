package com.alan.routineos.ui.features.node_type_manager.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.alan.routineos.ui.features.node_type_manager.components.schema.TypeChip
import com.alan.routineos.ui.features.node_type_manager.components.sheets.AddSchemaSheet
import com.alan.routineos.ui.features.node_type_manager.components.sheets.CreateTypeSheet
import com.alan.routineos.ui.features.node_type_manager.viewmodel.NodeTypeManagerViewModel
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
                    text = "TIPOS DE CAMPOS",
                    style = TitleNode.copy(fontSize = 12.sp, letterSpacing = 0.08.sp),
                    color = Color(0xFFE0E0E0)
                )

                Text(
                    "+ Tipo",
                    modifier = Modifier.clickable { showCreateTypeSheet = true },
                    style = TitleNode.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    color = Color(0xFF1565C0)
                )
            }
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            item {
                Text(
                    "TIPOS DEFINIDOS",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                    style = MetaMono.copy(fontSize = 9.sp),
                    color = Color(0xFF555555)
                )

                FlowRow(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    uiState.nodeTypes.forEach { type ->
                        val isSelected = uiState.selectedType?.id == type.id
                        TypeChip(
                            text = type.name,
                            isSelected = isSelected,
                            onClick = { viewModel.selectType(if (isSelected) null else type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.selectedType != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        color = Color(0xFF0D1F3A),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                uiState.selectedType!!.name.uppercase(),
                                style = TitleNode.copy(fontSize = 12.sp, letterSpacing = 0.06.sp),
                                color = Color(0xFF42A5F5)
                            )
                            Text(
                                "cerrar \u00d7",
                                modifier = Modifier.clickable { viewModel.selectType(null) },
                                style = TitleNode.copy(fontSize = 10.sp),
                                color = Color(0xFF1565C0)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "CAMPOS DEL TIPO",
                            style = MetaMono.copy(fontSize = 9.sp),
                            color = Color(0xFF555555)
                        )
                        Text(
                            "+ campo",
                            modifier = Modifier.clickable { showAddSchemaSheet = true },
                            style = TitleNode.copy(fontSize = 10.sp),
                            color = Color(0xFF1565C0)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .fillMaxWidth()
                            .clickable { showAddSchemaSheet = true }
                    ) {
                        LocalAddDashedButton(text = "nuevo campo")
                    }
                }
            } else {
                uiState.nodeTypes.forEach { type ->
                    item {
                        Text(
                            "VISTA PREVIA \u00b7 ${type.name.uppercase()}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                            style = MetaMono.copy(fontSize = 9.sp),
                            color = Color(0xFF555555)
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(
                                    Color(0xFF1E1E1E).copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                "Selecciona el tipo para editar sus campos",
                                style = TitleNode.copy(fontSize = 11.sp),
                                color = Color(0xFF444444)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
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
                    viewModel.addSchemaFull(
                        uiState.selectedType!!.id,
                        name,
                        label,
                        type,
                        default,
                        unit
                    )
                    showAddSchemaSheet = false
                }
            )
        }
    }
}

@Composable
fun LocalAddDashedButton(text: String, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFF444444),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text,
                style = TitleNode.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = Color(0xFF444444)
            )
        }
    }
}
