package com.alan.routineos.ui.screens.template_builder.sections

import androidx.compose.foundation.lazy.LazyListScope
import com.alan.routineos.ui.screens.template_builder.components.MetadataFieldItem
import com.alan.routineos.ui.screens.template_builder.components.SectionHeader

fun LazyListScope.metadataSection(
    onNavigateToTypeManager: () -> Unit
) {
    item {
        SectionHeader(title = "Campos personalizados", onAdd = onNavigateToTypeManager)
        
        // Muestra campos basados en el mockup
        MetadataFieldItem(type = "NUM", name = "series", value = "def: 4")
        MetadataFieldItem(type = "NUM", name = "reps", value = "def: 8")
        MetadataFieldItem(type = "NUM", name = "peso", value = "unidad: kg")
    }
}
