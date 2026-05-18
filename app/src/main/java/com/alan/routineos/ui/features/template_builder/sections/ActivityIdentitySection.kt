package com.alan.routineos.ui.features.template_builder.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.features.template_builder.components.BuilderTextField
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.ColorTextDim

@Composable
fun ActivityIdentitySection(
    name: String,
    onNameChange: (String) -> Unit,
    selectedCategory: ContextCategory,
    onCategoryChange: (ContextCategory) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
        Text(
            "¿QUÉ VAMOS A ORGANIZAR?",
            style = MetaMono.copy(fontSize = 9.sp, letterSpacing = 1.sp),
            color = ColorTextDim
        )

        Spacer(modifier = Modifier.height(16.dp))

        BuilderTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Ej. Push Day, Semestre 8, Meditación...",
            isFocused = name.isEmpty()
        )

        Spacer(modifier = Modifier.height(24.dp))

        ContextGrid(
            selected = selectedCategory,
            onSelected = onCategoryChange
        )
    }
}

@Composable
private fun ContextGrid(
    selected: ContextCategory,
    onSelected: (ContextCategory) -> Unit
) {
    val rows = ContextCategory.entries.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { category ->
                    val isSelected = selected == category
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) Color(0xFF1E1E1E) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                0.5.dp,
                                if (isSelected) Color(0xFF444444) else Color(0xFF222222),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelected(category) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.label,
                            style = MetaMono.copy(
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) Color.White else Color(0xFF666666)
                        )
                    }
                }
            }
        }
    }
}
