package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.primitivedevicestoic.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditorDialog(
    birthDate: Long?,
    sleepTime: String,
    isDefaultLauncher: Boolean,
    versionName: String,
    themeBg: Color,
    themeFg: Color,
    onDismiss: () -> Unit,
    onBirthDateClick: () -> Unit,
    onSleepTimeClick: () -> Unit,
    onSetDefaultLauncherClick: () -> Unit,
    onRateAppClick: () -> Unit,
    onBuyMeCoffeeClick: () -> Unit,
    settingsRow: @Composable (String, Color, @Composable () -> Unit) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeBg,
        titleContentColor = themeFg,
        textContentColor = themeFg.copy(alpha = 0.8f),
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                stringResource(R.string.editor_settings),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Normal)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                settingsRow(stringResource(R.string.birth_date), themeFg) {
                    Text(
                        text = birthDate?.let {
                            SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(it))
                        } ?: stringResource(R.string.set_label),
                        modifier = Modifier
                            .clickable { onBirthDateClick() }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeFg
                    )
                }
                settingsRow(
                    stringResource(R.string.sleep_time_label),
                    themeFg
                ) {
                    Text(
                        text = sleepTime,
                        modifier = Modifier
                            .clickable { onSleepTimeClick() }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeFg
                    )
                }
                if (!isDefaultLauncher) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onSetDefaultLauncherClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themeFg.copy(0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = themeFg)
                    ) {
                        Text(
                            stringResource(R.string.set_as_default),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRateAppClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, themeFg.copy(0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = themeFg)
                ) {
                    Text(
                        stringResource(R.string.rate_app),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onBuyMeCoffeeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, themeFg.copy(0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = themeFg)
                ) {
                    Text(
                        stringResource(R.string.buy_me_coffee),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "v$versionName",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = themeFg.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    stringResource(R.string.close),
                    color = themeFg,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    )
}
