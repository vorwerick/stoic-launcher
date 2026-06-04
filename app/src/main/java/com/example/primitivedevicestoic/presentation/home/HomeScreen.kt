package com.example.primitivedevicestoic.presentation.home

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.presentation.home.components.*
import org.koin.androidx.compose.koinViewModel
import java.text.Normalizer
import java.util.regex.Pattern
import androidx.core.net.toUri

private fun String.removeDiacritics(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalized).replaceAll("")
}

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val apps = uiState.apps
    val selectedApps = uiState.selectedApps
    val isEditorMode = uiState.isEditorMode
    val daysAlive = uiState.daysAlive
    val birthDate = uiState.birthDate
    val intention = uiState.intention
    val currentTime = uiState.currentTime
    val currentDate = uiState.currentDate
    val searchQuery = uiState.searchQuery
    val sleepTime = uiState.sleepTime
    val timeUntilSleep = uiState.timeUntilSleep
    val isDefaultLauncher = uiState.isDefaultLauncher
    val showEditorTip = uiState.showEditorTip
    val isDarkMode = uiState.isDarkMode
    val isMottoEnabled = uiState.isMottoEnabled
    val isSystemBarHidden = uiState.isSystemBarHidden
    val listMotto = uiState.listMotto
    val currentMottos = uiState.currentMottos

    val themeBg = if (isDarkMode) Color.Black else Color.White
    val themeFg = if (isDarkMode) Color.White else Color.Black
    val subtleColor = themeFg.copy(alpha = 0.12f)
    val secondaryText = themeFg.copy(alpha = 0.6f)
    val hintText = themeFg.copy(alpha = 0.4f)

    var showIntentionDialog by remember { mutableStateOf(false) }
    var intentionText by remember { mutableStateOf(intention) }

    LaunchedEffect(intention) { intentionText = intention }

    val selectedPackageNames by remember(selectedApps) {
        derivedStateOf { selectedApps.map { it.packageName }.toSet() }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.updateDefaultLauncherStatus() }

    LaunchedEffect(Unit) { viewModel.fullRefresh() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startPeriodicUpdates()
                viewModel.fullRefresh()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopPeriodicUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopPeriodicUpdates()
        }
    }

    val backgroundInteractionSource = remember { MutableInteractionSource() }

    val view = LocalView.current
    LaunchedEffect(isSystemBarHidden) {
        val window = (context as? android.app.Activity)?.window ?: return@LaunchedEffect
        val controller = WindowInsetsControllerCompat(window, view)
        if (isSystemBarHidden) {
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                interactionSource = backgroundInteractionSource,
                indication = null,
                onClick = {},
                onLongClick = { viewModel.setEditorMode(true) }
            ),
        containerColor = themeBg,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(themeBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    if (isSystemBarHidden) Modifier.padding(top = 24.dp)
                    else Modifier
                )
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeHeader(
                currentDate = currentDate,
                currentTime = currentTime,
                daysAlive = daysAlive,
                timeUntilSleep = timeUntilSleep,
                themeFg = themeFg,
                secondaryText = secondaryText
            )

            HorizontalDivider(color = subtleColor, thickness = 1.dp)

            if (showEditorTip && !isEditorMode) {
                Text(
                    text = stringResource(R.string.editor_tip),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Light
                    ),
                    color = hintText,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            IntentionSection(
                intention = intention,
                themeFg = themeFg,
                secondaryText = secondaryText,
                hintText = hintText,
                onClick = { showIntentionDialog = true }
            )

            HorizontalDivider(color = subtleColor, thickness = 0.5.dp)

            AppCarousel(
                selectedApps = selectedApps,
                isMottoEnabled = isMottoEnabled,
                listMotto = listMotto,
                currentMottos = currentMottos,
                themeFg = themeFg,
                secondaryText = secondaryText,
                onAppClick = { app ->
                    try {
                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                        if (intent != null) context.startActivity(intent)
                    } catch (e: Exception) {
                    }
                },
                onAppLongClick = { app -> viewModel.toggleAppSelection(app) },
                onAddAppClick = { isSearchActive = true }
            )

            if (!isSearchActive) {
                HomeBottomBar(
                    isDarkMode = isDarkMode,
                    themeFg = themeFg,
                    secondaryText = secondaryText,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onPhoneClick = {
                        val intent = Intent(Intent.ACTION_CALL_BUTTON)
                        context.startActivity(intent)
                    },
                    onSearchClick = { isSearchActive = true }
                )
            }
        }
    }

    if (isSearchActive) {
        SearchOverlay(
            searchQuery = searchQuery,
            apps = apps,
            selectedPackageNames = selectedPackageNames,
            themeBg = themeBg,
            themeFg = themeFg,
            subtleColor = subtleColor,
            secondaryText = secondaryText,
            hintText = hintText,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            onCloseSearch = {
                isSearchActive = false
                viewModel.setSearchQuery("")
            },
            onAppClick = { app ->
                try {
                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                        isSearchActive = false
                        viewModel.setSearchQuery("")
                    }
                } catch (e: Exception) {
                }
            },
            onToggleAppSelection = { viewModel.toggleAppSelection(it) },
            removeDiacritics = { removeDiacritics() }
        )
    }

    if (isEditorMode) {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName ?: "1.0"
        EditorDialog(
            birthDate = birthDate,
            sleepTime = sleepTime,
            isDefaultLauncher = isDefaultLauncher,
            versionName = versionName,
            isMottoEnabled = isMottoEnabled,
            onMottoToggle = { viewModel.toggleMottoEnabled() },
            isSystemBarHidden = isSystemBarHidden,
            onSystemBarToggle = { viewModel.toggleSystemBar() },
            themeBg = themeBg,
            themeFg = themeFg,
            onDismiss = { viewModel.setEditorMode(false) },
            onBirthDateClick = { showDatePicker = true },
            onSleepTimeClick = { showTimePicker = true },
            onSetDefaultLauncherClick = {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = context.getSystemService(RoleManager::class.java)
                    if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                        roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    } else Intent(Settings.ACTION_HOME_SETTINGS)
                } else Intent(Settings.ACTION_HOME_SETTINGS)
                launcher.launch(intent)
            },
            onRateAppClick = {
                val packageName = context.packageName
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
                } catch (e: ActivityNotFoundException) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()))
                }
            },
            onBuyMeCoffeeClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, context.getString(R.string.coffee_url).toUri()))
            },
            onAppInfoClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = "package:${context.packageName}".toUri()
                }
                context.startActivity(intent)
            },
            settingsRow = { label, fg, content -> SettingsRow(label, fg, content) }
        )
    }

    if (showIntentionDialog) {
        IntentionDialog(
            intentionText = intentionText,
            themeBg = themeBg,
            themeFg = themeFg,
            subtleColor = subtleColor,
            secondaryText = secondaryText,
            hintText = hintText,
            onIntentionTextChange = { intentionText = it },
            onDismiss = { showIntentionDialog = false },
            onConfirm = {
                viewModel.saveIntention(intentionText)
                showIntentionDialog = false
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setBirthDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val parts = sleepTime?.split(":")
        val initialHour = parts?.getOrNull(0)?.toIntOrNull() ?: 22
        val initialMinute = parts?.getOrNull(1)?.toIntOrNull() ?: 0
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setSleepTime(String.format("%02d:%02d", timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            containerColor = themeBg,
            titleContentColor = themeFg,
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state = timePickerState,
                        colors = if (isDarkMode) TimePickerDefaults.colors(
                            clockDialColor = Color.DarkGray,
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.Gray,
                            selectorColor = Color.White,
                            periodSelectorSelectedContainerColor = Color.DarkGray,
                            periodSelectorUnselectedContainerColor = Color.Black,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.Gray,
                            timeSelectorSelectedContainerColor = Color.DarkGray,
                            timeSelectorUnselectedContainerColor = Color.Black,
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = Color.Gray
                        ) else TimePickerDefaults.colors()
                    )
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(label: String, themeFg: Color, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = themeFg.copy(alpha = 0.85f)
        )
        content()
    }
}
