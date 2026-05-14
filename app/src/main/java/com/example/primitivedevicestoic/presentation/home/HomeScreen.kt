package com.example.primitivedevicestoic.presentation.home

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.graphics.drawable.toBitmap
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.domain.model.AppInfo
import com.example.primitivedevicestoic.ui.theme.White
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val batteryPercentage by viewModel.batteryPercentage.collectAsState()
    val isEditorMode by viewModel.isEditorMode.collectAsState()
    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val isOfflineMode by viewModel.isOfflineMode.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()
    val todayUnlockCount by viewModel.todayUnlockCount.collectAsState()
    val daysAlive by viewModel.daysAlive.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val quote by viewModel.quote.collectAsState()
    val intention by viewModel.intention.collectAsState()
    val sleepTime by viewModel.sleepTime.collectAsState()
    val timeUntilSleep by viewModel.timeUntilSleep.collectAsState()
    val hoursRemaining by viewModel.hoursRemaining.collectAsState()
    val timeSinceLastUse by viewModel.timeSinceLastUse.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showIntentionEdit by remember { mutableStateOf(false) }
    var intentionText by remember { mutableStateOf(intention) }
    
    LaunchedEffect(intention) {
        intentionText = intention
    }

    val selectedPackageNames by remember {
        derivedStateOf { selectedApps.map { it.packageName }.toSet() }
    }
    
    var showAppList by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.updateDefaultLauncherStatus()
    }

    val hasUsageStatsPermission = remember {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.noteOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    LaunchedEffect(Unit) {
        viewModel.fullRefresh()
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {},
                onLongClick = { viewModel.setEditorMode(true) }
            ),
        containerColor = Color.Black,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Color.Black)
            )
        },
        bottomBar = {
            Button(
                onClick = { showAppList = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = White
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, White)
            ) {
                Text(stringResource(R.string.app_list))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Rotating prompt: stoické citáty
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            // 2. Text: Do konce dnešního dne zbývá X hodin a minut do spánku, dnešní den je tvůj Y. v životě
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Do konce dne zbývá $hoursRemaining hodin a $timeUntilSleep do spánku",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
                daysAlive?.let {
                    Text(
                        text = stringResource(R.string.days_alive, it),
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                }
            }

            // 3. Možnost zadat si záměr dne, popis se záměrem a tlačítko edit
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.intention),
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
                if (showIntentionEdit) {
                    TextField(
                        value = intentionText,
                        onValueChange = { intentionText = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = White,
                            unfocusedTextColor = White
                        )
                    )
                    Button(
                        onClick = {
                            viewModel.saveIntention(intentionText)
                            showIntentionEdit = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.set_label))
                    }
                } else {
                    Text(
                        text = intention.ifEmpty { "..." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.edit),
                        color = White,
                        modifier = Modifier.clickable { showIntentionEdit = true },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // 4. Čas uplynulý od posledního použití telefonu
            Text(
                text = stringResource(R.string.time_since_last_use, timeSinceLastUse),
                style = MaterialTheme.typography.bodySmall,
                color = White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, White)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
                ) {
                    items(10) { index -> // 2 řady po 5 pro "Rychlé aplikace"
                        val app = selectedApps.getOrNull(index)
                        AppIconItem(
                            app = app,
                            onClick = {
                                if (app != null) {
                                    try {
                                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (intent != null) {
                                            context.startActivity(intent)
                                        }
                                    } catch (e: Exception) {}
                                } else {
                                    isSelectionMode = true
                                    showAppList = true
                                }
                            },
                            onLongClick = {
                                if (app != null) {
                                    viewModel.toggleAppSelection(app)
                                } else {
                                    isSelectionMode = true
                                    showAppList = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (isEditorMode) {
        AlertDialog(
            onDismissRequest = { viewModel.setEditorMode(false) },
            containerColor = Color.DarkGray,
            titleContentColor = White,
            textContentColor = White,
            title = { Text(stringResource(R.string.editor_settings)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.evening_reminder))
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { viewModel.setReminderEnabled(it) }
                        )
                    }
                    if (isReminderEnabled) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.reminder_time))
                            Text(
                                text = reminderTime,
                                modifier = Modifier
                                    .clickable {
                                        val nextTime = if (reminderTime == "21:00") "22:00" else "21:00"
                                        viewModel.setReminderTime(nextTime)
                                    }
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Čas spánku")
                        Text(
                            text = sleepTime,
                            modifier = Modifier
                                .clickable {
                                    // Pro jednoduchost budeme cyklit mezi 21:00, 22:00, 23:00, 00:00
                                    val nextTime = when (sleepTime) {
                                        "21:00" -> "22:00"
                                        "22:00" -> "23:00"
                                        "23:00" -> "00:00"
                                        else -> "21:00"
                                    }
                                    viewModel.setSleepTime(nextTime)
                                }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.birth_date))
                        Text(
                            text = birthDate?.let { SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(it)) } ?: stringResource(R.string.set_label),
                            modifier = Modifier
                                .clickable { showDatePicker = true }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (!isDefaultLauncher) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val roleManager = context.getSystemService(RoleManager::class.java)
                                    if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                                        roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                                    } else {
                                        Intent(Settings.ACTION_HOME_SETTINGS)
                                    }
                                } else {
                                    Intent(Settings.ACTION_HOME_SETTINGS)
                                }
                                launcher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = White,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(stringResource(R.string.set_as_default))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setEditorMode(false) }) {
                    Text(stringResource(R.string.close), color = White)
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.setBirthDate(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
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

    if (showAppList) {
        ModalBottomSheet(
            onDismissRequest = { 
                showAppList = false
                isSelectionMode = false
                viewModel.setSearchQuery("")
            }
        ) {
            val sortedApps by remember(apps, searchQuery) {
                derivedStateOf { 
                    apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
                        .sortedBy { it.label.lowercase() } 
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(horizontal = 16.dp)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text(stringResource(R.string.what_looking_for), color = White.copy(alpha = 0.5f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = White,
                        focusedIndicatorColor = White,
                        unfocusedIndicatorColor = White.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(
                        items = sortedApps,
                        key = { it.packageName }
                    ) { app ->
                        val isSelected = selectedPackageNames.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelectionMode) {
                                        viewModel.toggleAppSelection(app)
                                    } else {
                                        try {
                                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                            if (intent != null) {
                                                context.startActivity(intent)
                                                showAppList = false
                                                viewModel.setSearchQuery("")
                                            }
                                        } catch (e: Exception) {
                                            // Silent fail
                                        }
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected && isSelectionMode) MaterialTheme.colorScheme.primary else White,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelectionMode && isSelected) {
                                Text(stringResource(R.string.selected), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    app: AppInfo?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (app == null) Color.DarkGray else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (app?.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                )
            } else if (app == null) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_app),
                    tint = White
                )
            }
        }
    }
}
