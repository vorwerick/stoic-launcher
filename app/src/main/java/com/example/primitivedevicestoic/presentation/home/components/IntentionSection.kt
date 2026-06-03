package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primitivedevicestoic.R

@Composable
fun IntentionSection(
    intention: String,
    themeFg: Color,
    secondaryText: Color,
    hintText: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.intention).lowercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            ),
            color = secondaryText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = intention.ifEmpty { stringResource(R.string.set_intention_hint) },
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light),
            color = if (intention.isEmpty()) hintText else themeFg,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp)
        )
    }
}
