package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.primitivedevicestoic.R

@Composable
fun IntentionDialog(
    intentionText: String,
    themeBg: Color,
    themeFg: Color,
    subtleColor: Color,
    secondaryText: Color,
    hintText: Color,
    onIntentionTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeBg,
        title = {
            Text(
                text = stringResource(R.string.intention),
                color = themeFg,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            TextField(
                value = intentionText,
                onValueChange = onIntentionTextChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = themeFg),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = themeFg,
                    unfocusedTextColor = themeFg,
                    cursorColor = themeFg,
                    focusedIndicatorColor = themeFg,
                    unfocusedIndicatorColor = subtleColor
                ),
                singleLine = true,
                placeholder = {
                    Text(
                        stringResource(R.string.set_intention_hint),
                        color = hintText
                    )
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.set_label),
                    color = themeFg,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(android.R.string.cancel),
                    color = secondaryText
                )
            }
        }
    )
}
