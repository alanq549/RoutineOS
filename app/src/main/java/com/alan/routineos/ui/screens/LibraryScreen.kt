package com.alan.routineos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alan.routineos.data.local.entities.RoutineTemplate
import com.alan.routineos.ui.theme.*
import com.alan.routineos.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    onNavigateToBuilder: (String) -> Unit,
    onNavigateToTypes: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(ColorSurface).statusBarsPadding()) {
                CenterAlignedTopAppBar(
                    title = { Text("LIBRERÍA", style = MetaMono, color = ColorText) },
                    actions = {
                        TextButton(onClick = onNavigateToTypes) {
                            Text("TIPOS", style = MetaMono, color = ColorExec)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = ColorSurface)
                )
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToBuilder("new") },
                containerColor = ColorExec,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Template")
            }
        },
        containerColor = ColorBg
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorExec)
            }
        } else if (uiState.templates.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No hay plantillas guardadas", color = ColorTextDim, style = TitleNode)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.templates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onEdit = { onNavigateToBuilder(template.id) },
                        onUse = { viewModel.useTemplateToday(template) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        placeholder = { Text("Buscar rutina...", style = TitleNode, color = ColorTextMuted) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ColorTextDim) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = ColorBg,
            focusedContainerColor = ColorBg,
            unfocusedBorderColor = ColorBorder,
            focusedBorderColor = ColorExec
        ),
        singleLine = true
    )
}

@Composable
fun TemplateCard(
    template: RoutineTemplate,
    onEdit: () -> Unit,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = try { Color(android.graphics.Color.parseColor(template.colorHex)) } 
                                catch (e: Exception) { ColorExec }, 
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, style = TitleNode, color = ColorText)
                Text("Plantilla", style = MetaMono, color = ColorTextDim)
            }
            IconButton(
                onClick = onUse,
                colors = IconButtonDefaults.iconButtonColors(containerColor = ColorExec.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Use today", tint = ColorExec)
            }
        }
    }
}
