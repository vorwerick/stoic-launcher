package com.example.primitivedevicestoic.presentation.home

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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

private data class AnswerData(val text: String, val correct: Boolean)
private data class QuestionData(val question: String, val answers: List<AnswerData>)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val apps by viewModel.apps.collectAsState()
    val selectedApps by viewModel.selectedApps.collectAsState()
    val isEditorMode by viewModel.isEditorMode.collectAsState()
    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()
    val daysAlive by viewModel.daysAlive.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val quote by viewModel.quote.collectAsState()
    val intention by viewModel.intention.collectAsState()
    val sleepTime by viewModel.sleepTime.collectAsState()
    val timeUntilSleep by viewModel.timeUntilSleep.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

    var showIntentionEdit by remember { mutableStateOf(false) }
    var intentionText by remember { mutableStateOf(intention) }
    var isLocked by remember { mutableStateOf(true) }

    LaunchedEffect(intention) { intentionText = intention }

    val stoicQuestions = remember {
        listOf(
            QuestionData(
                "Co je v tvé moci?",
                listOf(
                    AnswerData("Moje myšlenky a činy", true),
                    AnswerData("Reakce ostatních", false),
                    AnswerData("Výsledky mého úsilí", false)
                ).shuffled()
            ),
            QuestionData(
                "Jak stoici přistupují k překážkám?",
                listOf(
                    AnswerData("Překážka se stává cestou", true),
                    AnswerData("Vyhýbají se jim za každou cenu", false),
                    AnswerData("Hledají vnější pomoc", false)
                ).shuffled()
            ),
            QuestionData(
                "Co je základem stoického myšlení?",
                listOf(
                    AnswerData("Rozlišit, co závisí na nás", true),
                    AnswerData("Ovládat všechny situace", false),
                    AnswerData("Potlačit všechny emoce", false)
                ).shuffled()
            )
        )
    }
    val currentQuestion = remember { stoicQuestions.random() }

    val selectedPackageNames by remember {
        derivedStateOf { selectedApps.map { it.packageName }.toSet() }
    }

    var showAppList by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { viewModel.updateDefaultLauncherStatus() }

    LaunchedEffect(Unit) { viewModel.fullRefresh() }

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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.edit),
                        color = White,
                        modifier = Modifier.clickable { showIntentionEdit = true },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Do spánku: $timeUntilSleep",
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isLocked) Modifier.blur(18.dp) else Modifier)
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(10) { index ->
                            val app = selectedApps.getOrNull(index)
                            VerticalAppItem(
                                app = app,
                                onClick = {
                                    if (app != null) {
                                        try {
                                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                            if (intent != null) context.startActivity(intent)
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

                    Button(
                        onClick = { showAppList = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, White)
                    ) {
                        Text(stringResource(R.string.app_list))
                    }
                }

                if (isLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { }
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium,
                            color = White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        currentQuestion.answers.forEach { answer ->
                            Button(
                                onClick = { if (answer.correct) isLocked = false },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black.copy(alpha = 0.55f),
                                    contentColor = White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, White)
                            ) {
                                Text(answer.text)
                            }
                        }
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Čas spánku")
                        Text(
                            text = sleepTime,
                            modifier = Modifier
                                .clickable {
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.birth_date))
                        Text(
                            text = birthDate?.let {
                                SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(it))
                            } ?: stringResource(R.string.set_label),
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
                            colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Color.Black)
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    placeholder = {
                        Text(stringResource(R.string.what_looking_for), color = White.copy(alpha = 0.5f))
                    },
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

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(items = sortedApps, key = { it.packageName }) { app ->
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
                                        } catch (e: Exception) {}
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
fun VerticalAppItem(
    app: AppInfo?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
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
        Text(
            text = app?.label ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
