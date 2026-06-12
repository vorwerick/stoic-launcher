package com.example.primitivedevicestoic.presentation.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primitivedevicestoic.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingHeader(text: String, color: Color) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = color.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun EditorDialog(
    birthDate: Long?,
    sleepTime: String?,
    isDefaultLauncher: Boolean,
    versionName: String,
    isDarkMode: Boolean,
    onDarkModeToggle: () -> Unit,
    isSystemBarHidden: Boolean,
    onSystemBarToggle: () -> Unit,
    themeBg: Color,
    themeFg: Color,
    onDismiss: () -> Unit,
    onBirthDateClick: () -> Unit,
    onSleepTimeClick: () -> Unit,
    onSetDefaultLauncherClick: () -> Unit,
    onRateAppClick: () -> Unit,
    onBuyMeCoffeeClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    isCallsEnabled: Boolean,
    onCallsToggle: () -> Unit,
    isMessagesEnabled: Boolean,
    onMessagesToggle: () -> Unit,
    isCameraEnabled: Boolean,
    onCameraToggle: () -> Unit,
    isSettingsEnabled: Boolean,
    onSettingsToggle: () -> Unit,
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
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingHeader(stringResource(R.string.settings_group_general), themeFg)
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
                settingsRow(stringResource(R.string.sleep_time_label), themeFg) {
                    Text(
                        text = sleepTime ?: stringResource(R.string.set_label),
                        modifier = Modifier
                            .clickable { onSleepTimeClick() }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = themeFg
                    )
                }

                SettingHeader(stringResource(R.string.settings_group_display), themeFg)
                settingsRow(stringResource(R.string.dark_mode), themeFg) {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { onDarkModeToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeFg,
                            checkedTrackColor = themeFg.copy(alpha = 0.5f),
                            uncheckedThumbColor = themeFg.copy(alpha = 0.5f),
                            uncheckedTrackColor = themeFg.copy(alpha = 0.1f)
                        )
                    )
                }
                settingsRow(stringResource(R.string.hide_system_bar), themeFg) {
                    Switch(
                        checked = isSystemBarHidden,
                        onCheckedChange = { onSystemBarToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeFg,
                            checkedTrackColor = themeFg.copy(alpha = 0.5f),
                            uncheckedThumbColor = themeFg.copy(alpha = 0.5f),
                            uncheckedTrackColor = themeFg.copy(alpha = 0.1f)
                        )
                    )
                }

                SettingHeader(stringResource(R.string.settings_group_shortcuts), themeFg)
                settingsRow(stringResource(R.string.show_calls), themeFg) {
                    Switch(
                        checked = isCallsEnabled,
                        onCheckedChange = { onCallsToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeFg,
                            checkedTrackColor = themeFg.copy(alpha = 0.5f),
                            uncheckedThumbColor = themeFg.copy(alpha = 0.5f),
                            uncheckedTrackColor = themeFg.copy(alpha = 0.1f)
                        )
                    )
                }
                settingsRow(stringResource(R.string.show_messages), themeFg) {
                    Switch(
                        checked = isMessagesEnabled,
                        onCheckedChange = { onMessagesToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeFg,
                            checkedTrackColor = themeFg.copy(alpha = 0.5f),
                            uncheckedThumbColor = themeFg.copy(alpha = 0.5f),
                            uncheckedTrackColor = themeFg.copy(alpha = 0.1f)
                        )
                    )
                }
                settingsRow(stringResource(R.string.show_camera), themeFg) {
                    Switch(
                        checked = isCameraEnabled,
                        onCheckedChange = { onCameraToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeFg,
                            checkedTrackColor = themeFg.copy(alpha = 0.5f),
                            uncheckedThumbColor = themeFg.copy(alpha = 0.5f),
                            uncheckedTrackColor = themeFg.copy(alpha = 0.1f)
                        )
                    )
                }
                settingsRow(stringResource(R.string.show_settings), themeFg) {
                    Switch(
                        checked = isSettingsEnabled,
                        onCheckedChange = { onSettingsToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeFg,
                            checkedTrackColor = themeFg.copy(alpha = 0.5f),
                            uncheckedThumbColor = themeFg.copy(alpha = 0.5f),
                            uncheckedTrackColor = themeFg.copy(alpha = 0.1f)
                        )
                    )
                }

                SettingHeader(stringResource(R.string.settings_group_about), themeFg)
                if (!isDefaultLauncher) {
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

                OutlinedButton(
                    onClick = onAppInfoClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, themeFg.copy(0.2f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = themeFg)
                ) {
                    Text(
                        stringResource(R.string.app_info),
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
