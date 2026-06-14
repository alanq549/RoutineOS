package com.alan.routineos.ui.features.template_builder.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.*

@Composable
fun SectionHeader(
    title: String, 
    addLabel: String = "+ agregar",
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MetaMono.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = ColorTextDim
        )
        Text(
            text = addLabel,
            modifier = Modifier.clickable { onAdd() },
            style = TitleNode.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = ColorExec
        )
    }
}
