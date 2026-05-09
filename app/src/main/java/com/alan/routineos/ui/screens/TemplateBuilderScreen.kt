package com.alan.routineos.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.data.local.entities.Node
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.TemplateBuilderViewModel

val TemplateColors = listOf(
    "#3FB950", // Verde RoutineOS
    "#58A6FF", // Azul Plan
    "#D29922", // Ámbar Pending
    "#F85149", // Rojo
    "#8957E5", // Púrpura
    "#E6EDF3"  // Blanco/Gris
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateBuilderScreen(
    onBack: () -> Unit,
    viewModel: TemplateBuilderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddNodeSheet by remember { mutableStateOf(false) }
    var selectedParentId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EDITOR DE RUTINA", style = MetaMono) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ColorExec)
                    } else {
                        Button(
                            onClick = { 
                                viewModel.saveTemplate()
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                            enabled = uiState.name.isNotBlank() && uiState.nodes.isNotEmpty()
                        ) {
                            Text("GUARDAR", style = MetaMono)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ColorBg)
            )
        },
        containerColor = ColorBg
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorExec)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Info básica
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("Nombre de la rutina") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de color
                Text("COLOR", style = MetaMono, color = ColorTextDim)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TemplateColors.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(android.graphics.Color.parseColor(colorHex)), CircleShape)
                                .border(
                                    width = if (uiState.colorHex == colorHex) 2.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.updateColor(colorHex) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("ESTRUCTURA (MÁX. 3 NIVELES)", style = MetaMono, color = ColorTextDim)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    val rootNodes = uiState.nodes.filter { it.parentId == null }
                    items(rootNodes, key = { it.id }) { node ->
                        EditableNodeItem(
                            node = node,
                            allNodes = uiState.nodes,
                            depth = 0,
                            onAddChild = { 
                                selectedParentId = it
                                showAddNodeSheet = true
                            },
                            onDelete = viewModel::deleteNode,
                            onUpdateName = viewModel::updateNodeName
                        )
                    }
                    
                    item {
                        if (rootNodes.isEmpty()) {
                            Button(
                                onClick = { 
                                    selectedParentId = null
                                    showAddNodeSheet = true
                                },
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSurface2)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar bloque raíz")
                            }
                        }
                    }
                }
            }
        }

        if (showAddNodeSheet) {
            AddNodeToTemplateSheet(
                nodeTypes = uiState.nodeTypes,
                onDismiss = { showAddNodeSheet = false },
                onAdd = { name, typeId ->
                    viewModel.addNode(name, typeId, selectedParentId)
                    showAddNodeSheet = false
                }
            )
        }
    }
}

@Composable
fun EditableNodeItem(
    node: Node,
    allNodes: List<Node>,
    depth: Int,
    onAddChild: (String) -> Unit,
    onDelete: (String) -> Unit,
    onUpdateName: (String, String) -> Unit
) {
    val children = allNodes.filter { it.parentId == node.id }
    var isEditingName by remember { mutableStateOf(false) }
    var nameText by remember { mutableStateOf(node.name) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(ColorSurface, RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditingName) {
                TextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TitleNode,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = { 
                            onUpdateName(node.id, nameText)
                            isEditingName = false
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "OK")
                        }
                    }
                )
            } else {
                Text(
                    text = node.name,
                    modifier = Modifier.weight(1f).clickable { isEditingName = true },
                    style = TitleNode,
                    color = ColorText
                )
            }

            // Restricción: Máximo 3 niveles (0, 1, 2)
            if (depth < 2) {
                IconButton(onClick = { onAddChild(node.id) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = ColorPlan, modifier = Modifier.size(20.dp))
                }
            }
            
            IconButton(onClick = { onDelete(node.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            }
        }

        children.forEach { child ->
            EditableNodeItem(child, allNodes, depth + 1, onAddChild, onDelete, onUpdateName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNodeToTemplateSheet(
    nodeTypes: List<com.alan.routineos.data.local.entities.NodeType>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTypeId by remember { mutableStateOf(nodeTypes.firstOrNull()?.id ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = ColorSurface) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("NUEVO BLOQUE", style = MetaMono, color = ColorText)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ColorExec)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("TIPO", style = MetaMono, color = ColorTextDim)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                nodeTypes.forEach { type ->
                    FilterChip(
                        selected = selectedTypeId == type.id,
                        onClick = { selectedTypeId = type.id },
                        label = { Text(type.name) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { onAdd(name, selectedTypeId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ColorExec),
                enabled = name.isNotBlank() && selectedTypeId.isNotBlank()
            ) {
                Text("AGREGAR")
            }
        }
    }
}
