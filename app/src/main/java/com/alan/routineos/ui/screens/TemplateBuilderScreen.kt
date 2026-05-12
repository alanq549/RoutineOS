package com.alan.routineos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.TemplateBuilderViewModel

@Composable
fun TemplateBuilderScreen(
    onBack: () -> Unit,
    onNavigateToTypeManager: () -> Unit,
    viewModel: TemplateBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val colors = listOf(
        Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFC107),
        Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0),
        Color(0xFF795548), Color(0xFF607D8B)
    )

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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF777777), modifier = Modifier.size(16.dp))
                }
                
                Text(
                    text = if (uiState.templateId == null) "NUEVA ACTIVIDAD" else "EDITAR ACTIVIDAD",
                    style = TitleNode.copy(fontSize = 12.sp, letterSpacing = 0.08.sp),
                    color = Color(0xFFE0E0E0)
                )
                
                Surface(
                    onClick = { viewModel.saveTemplate() },
                    color = if (uiState.name.isNotBlank()) Color(0xFF1565C0) else Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "GUARDAR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = TitleNode.copy(fontSize = 11.sp),
                        color = if (uiState.name.isNotBlank()) Color.White else Color(0xFF444444)
                    )
                }
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
                // Campo Nombre
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text("NOMBRE", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(4.dp))
                    BuilderTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        placeholder = "Ej: Push Day",
                        isFocused = true
                    )
                }
            }

            item {
                // Selector de Color
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text("COLOR", style = MetaMono.copy(fontSize = 9.sp), color = Color(0xFF555555))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { color ->
                            val colorHex = String.format("#%06X", color.toArgb() and 0xFFFFFF).uppercase()
                            val isSelected = uiState.colorHex.uppercase() == colorHex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(color, CircleShape)
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, Color(0xFF121212), CircleShape).border(4.dp, Color.White, CircleShape).padding(4.dp)
                                        else Modifier
                                    )
                                    .clickable { viewModel.updateColor(colorHex) }
                            )
                        }
                    }
                }
            }

            item {
                // Sección Metadata
                SectionHeader(title = "Campos personalizados", onAdd = onNavigateToTypeManager)
                
                // Muestra campos basados en el mockup
                MetadataFieldItem(type = "NUM", name = "series", value = "def: 4")
                MetadataFieldItem(type = "NUM", name = "reps", value = "def: 8")
                MetadataFieldItem(type = "NUM", name = "peso", value = "unidad: kg")
            }

            item {
                // Sección Estructura
                SectionHeader(title = "Estructura de nodos", onAdd = { /* AGREGAR RAÍZ */ })
            }

            // Muestra una estructura fija para coincidir con el diseño si no hay nodos reales,
            // o usa los reales si existen.
            if (uiState.nodes.isEmpty()) {
                item {
                    MockNodeItem("Pecho", depth = 0, hasChildren = true)
                    MockNodeItem("Press banca", depth = 1, meta = "4×8")
                    MockNodeItem("Press inclinado", depth = 1, meta = "3×10")
                    MockNodeItem("Hombro", depth = 0, hasChildren = true)
                    MockNodeItem("Elevaciones laterales", depth = 1, meta = "3×15")
                }
            } else {
                val rootNodes = uiState.nodes.filter { it.parentId == null }
                items(rootNodes) { node ->
                    BuilderNodeHierarchy(
                        node = node,
                        allNodes = uiState.nodes,
                        depth = 0
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                AddDashedButton(text = "agregar grupo")
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title.uppercase(), style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium), color = Color(0xFF555555))
        Text(
            "+ agregar",
            modifier = Modifier.clickable { onAdd() },
            style = TitleNode.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
            color = Color(0xFF1565C0)
        )
    }
}

@Composable
fun BuilderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isFocused: Boolean = false
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TitleNode.copy(color = Color(0xFFE0E0E0), fontSize = 13.sp),
        cursorBrush = SolidColor(Color(0xFF1565C0)),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .border(
                width = 0.5.dp,
                color = if (isFocused) Color(0xFF1565C0) else Color(0xFF2A2A2A),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) Text(placeholder, color = Color(0xFF444444), style = TitleNode.copy(fontSize = 13.sp))
            innerTextField()
        }
    )
}

@Composable
fun MetadataFieldItem(type: String, name: String, value: String) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .fillMaxWidth(),
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bgColor = when(type) {
                "NUM" -> Color(0xFF0D1F3A)
                "TXT" -> Color(0xFF0D2010)
                "BOOL" -> Color(0xFF1A1208)
                else -> Color(0xFF1A0D2A)
            }
            val txtColor = when(type) {
                "NUM" -> Color(0xFF42A5F5)
                "TXT" -> Color(0xFF4CAF50)
                "BOOL" -> Color(0xFFFF9800)
                else -> Color(0xFFCE93D8)
            }
            
            Surface(color = bgColor, shape = RoundedCornerShape(5.dp)) {
                Text(
                    type,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MetaMono.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = txtColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, modifier = Modifier.weight(1f), style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = Color(0xFFE0E0E0))
            Text(value, style = TitleNode.copy(fontSize = 10.sp), color = Color(0xFF444444))
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(24.dp).background(Color(0xFF1A1A1A), RoundedCornerShape(5.dp)).border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(5.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFF553333), modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
fun BuilderNodeHierarchy(node: Node, allNodes: List<Node>, depth: Int) {
    val children = allNodes.filter { it.parentId == node.id }
    MockNodeItem(node.name, depth, children.isNotEmpty())
    children.forEach { child ->
        BuilderNodeHierarchy(child, allNodes, depth + 1)
    }
}

@Composable
fun MockNodeItem(name: String, depth: Int, hasChildren: Boolean = false, meta: String? = null) {
    val bgColor = when(depth) {
        0 -> Color(0xFF1E1E1E)
        1 -> Color(0xFF181818)
        else -> Color(0xFF161616)
    }
    
    Surface(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 2.5.dp)
            .padding(start = (depth * 30).dp)
            .fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, if(depth == 0) Color(0xFF2A2A2A) else Color(0xFF222222))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(22.dp),
                color = if (depth == 0) Color(0xFF0D1F3A) else Color(0xFF1A1A1A),
                shape = RoundedCornerShape(5.dp),
                border = if (depth > 0) androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A)) else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (depth == 0) Icons.Default.Folder else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (depth == 0) Color(0xFF1565C0) else Color(0xFF555555),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(name, modifier = Modifier.weight(1f), style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = Color(0xFFE0E0E0))
            
            if (meta != null) {
                Text(meta, style = TitleNode.copy(fontSize = 10.sp), color = Color(0xFF444444), modifier = Modifier.padding(horizontal = 4.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (depth == 0) {
                    Box(
                        modifier = Modifier.size(24.dp).background(Color(0xFF1A1A1A), RoundedCornerShape(5.dp)).border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color(0xFF1565C0), modifier = Modifier.size(12.dp))
                    }
                }
                Box(
                    modifier = Modifier.size(24.dp).background(Color(0xFF1A1A1A), RoundedCornerShape(5.dp)).border(0.5.dp, Color(0xFF2A2A2A), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFF553333), modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
fun AddDashedButton(text: String) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier.padding(9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF444444), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, style = TitleNode.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = Color(0xFF444444))
        }
    }
}
