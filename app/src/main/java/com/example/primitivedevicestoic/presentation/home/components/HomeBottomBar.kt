package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeBottomBar(
    isDarkMode: Boolean,
    themeFg: Color,
    secondaryText: Color,
    onToggleDarkMode: () -> Unit,
    onPhoneClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 16.dp)
            .background(
                color = themeFg.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Přepínač Dark Mode
        IconButton(
            onClick = onToggleDarkMode
        ) {
            Icon(
                if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle dark mode",
                tint = secondaryText,
                modifier = Modifier.size(24.dp)
            )
        }

        // Ikona telefonu
        IconButton(
            onClick = onPhoneClick
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Open dialer",
                tint = secondaryText,
                modifier = Modifier.size(24.dp)
            )
        }

        // Search ikona
        IconButton(
            onClick = onSearchClick
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search apps",
                tint = secondaryText,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
