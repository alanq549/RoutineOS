package com.alan.routineos.ui.features.template_builder.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.ui.theme.MetaMono
import com.alan.routineos.ui.theme.TitleNode

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
