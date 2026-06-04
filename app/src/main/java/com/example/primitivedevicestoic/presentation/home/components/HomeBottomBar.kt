package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HomeBottomBar(
    themeFg: Color,
    secondaryText: Color,
    isCallsEnabled: Boolean,
    isMessagesEnabled: Boolean,
    isCameraEnabled: Boolean,
    isSettingsEnabled: Boolean,
    onPhoneClick: () -> Unit,
    onMessagesClick: () -> Unit,
    onCameraClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikona telefonu
        if (isCallsEnabled) {
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
        }

        // Ikona zpráv
        if (isMessagesEnabled) {
            IconButton(
                onClick = onMessagesClick
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Open messages",
                    tint = secondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Ikona fotoaparátu
        if (isCameraEnabled) {
            IconButton(
                onClick = onCameraClick
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Open camera",
                    tint = secondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Ikona nastavení
        if (isSettingsEnabled) {
            IconButton(
                onClick = onSettingsClick
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Open settings",
                    tint = secondaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
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
