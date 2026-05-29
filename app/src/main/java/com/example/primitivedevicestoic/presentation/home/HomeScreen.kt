package com.example.primitivedevicestoic.presentation.home

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.domain.model.AppInfo
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

private data class AnswerData(val text: String, val correct: Boolean)
private data class QuestionData(val question: String, val answers: List<AnswerData>)

private val allStoicQuestions = listOf(
    QuestionData("Co je v tvé moci?", listOf(
        AnswerData("Moje myšlenky a činy", true),
        AnswerData("Reakce ostatních", false),
        AnswerData("Výsledky mého úsilí", false)
    )),
    QuestionData("Jak stoici přistupují k překážkám?", listOf(
        AnswerData("Překážka se stává cestou", true),
        AnswerData("Vyhýbají se jim za každou cenu", false),
        AnswerData("Hledají pomoc zvenčí", false)
    )),
    QuestionData("Co je základem stoického myšlení?", listOf(
        AnswerData("Rozlišit, co závisí na nás", true),
        AnswerData("Ovládat všechny situace", false),
        AnswerData("Potlačit všechny emoce", false)
    )),
    QuestionData("Co znamená 'amor fati'?", listOf(
        AnswerData("Láska k osudu", true),
        AnswerData("Strach ze smrti", false),
        AnswerData("Touha po slávě", false)
    )),
    QuestionData("Kdo napsal Hovory k sobě?", listOf(
        AnswerData("Marcus Aurelius", true),
        AnswerData("Seneca", false),
        AnswerData("Epiktétos", false)
    )),
    QuestionData("Co říká 'dichotomie kontroly'?", listOf(
        AnswerData("Řiď jen to, co závisí na tobě", true),
        AnswerData("Kontroluj vše kolem sebe", false),
        AnswerData("Vzdej se veškeré kontroly", false)
    )),
    QuestionData("Co je 'memento mori'?", listOf(
        AnswerData("Připomínka smrtelnosti", true),
        AnswerData("Oslava života", false),
        AnswerData("Druh meditace", false)
    )),
    QuestionData("Jak stoici přistupují k bohatství?", listOf(
        AnswerData("Je lhostejné, záleží na ctnosti", true),
        AnswerData("Je nejvyšším cílem", false),
        AnswerData("Je zdrojem neštěstí", false)
    )),
    QuestionData("Co je 'eudaimonia' pro stoiky?", listOf(
        AnswerData("Dobře žít skrze ctnost", true),
        AnswerData("Maximalizovat požitek", false),
        AnswerData("Vyhnout se utrpení", false)
    )),
    QuestionData("Epiktétos byl původně...", listOf(
        AnswerData("Otrok", true),
        AnswerData("Císař", false),
        AnswerData("Vojevůdce", false)
    )),
    QuestionData("Jak stoici vnímají čas?", listOf(
        AnswerData("Žij přítomným okamžikem", true),
        AnswerData("Plánuj daleko dopředu", false),
        AnswerData("Lituji minulosti", false)
    )),
    QuestionData("Co je 'premeditatio malorum'?", listOf(
        AnswerData("Předvídání a přijetí možných ztrát", true),
        AnswerData("Vyhýbání se negativním myšlenkám", false),
        AnswerData("Modlitba za lepší časy", false)
    )),
    QuestionData("Která ze ctností není stoická?", listOf(
        AnswerData("Popularita", true),
        AnswerData("Moudrost", false),
        AnswerData("Spravedlnost", false)
    )),
    QuestionData("Co Marcus Aurelius říká o ranním vstávání?", listOf(
        AnswerData("Vstaň a konej svou práci", true),
        AnswerData("Odpočinek je klíčem ke ctnosti", false),
        AnswerData("Medituj před vstáváním", false)
    )),
    QuestionData("Co je klíčem ke stoické svobodě?", listOf(
        AnswerData("Nezávislost na vnějších věcech", true),
        AnswerData("Materiální zajištění", false),
        AnswerData("Souhlas ostatních", false)
    )),
)

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

    LaunchedEffect(intention) { intentionText = intention }

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

    val subtleWhite = Color.Black.copy(alpha = 0.12f)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(onClick = {}, onLongClick = { viewModel.setEditorMode(true) }),
        containerColor = Color.White,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 22.dp)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Citát
            Text(
                text = "\u201E$quote\u201C",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = Color.Black.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 16.dp)
            )

            HorizontalDivider(color = subtleWhite, thickness = 0.5.dp)

            // Záměr
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ZÁMĚR DNE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.Black.copy(alpha = 0.32f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (showIntentionEdit) {
                    TextField(
                        value = intentionText,
                        onValueChange = { intentionText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            cursorColor = Color.Black,
                            focusedIndicatorColor = subtleWhite,
                            unfocusedIndicatorColor = subtleWhite
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = {
                        viewModel.saveIntention(intentionText)
                        showIntentionEdit = false
                    }) {
                        Text(
                            stringResource(R.string.set_label),
                            color = Color.Black.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                } else {
                    Text(
                        text = intention.ifEmpty { "Nastav svůj záměr..." },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                        color = if (intention.isEmpty()) Color.Black.copy(alpha = 0.28f) else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showIntentionEdit = true }
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = subtleWhite, thickness = 0.5.dp)

            // Statistiky
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "DO SPÁNKU",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                        color = Color.Black.copy(alpha = 0.32f)
                    )
                    Text(
                        text = timeUntilSleep,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                        color = Color.Black
                    )
                }
                daysAlive?.let {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "DNÍ NAŽIVU",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                            color = Color.Black.copy(alpha = 0.32f)
                        )
                        Text(
                            text = "$it",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                            color = Color.Black
                        )
                    }
                }
            }

            HorizontalDivider(color = subtleWhite, thickness = 0.5.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Oblast aplikací
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(0.5.dp, subtleWhite, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(2) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(5) { col ->
                                    val index = row * 5 + col
                                    val app = selectedApps.getOrNull(index)
                                    GridAppItem(
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
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showAppList = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.app_list),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Black.copy(alpha = 0.28f)
                        )
                    }
                }
            }
        }
    }

    if (isEditorMode) {
        AlertDialog(
            onDismissRequest = { viewModel.setEditorMode(false) },
            containerColor = Color(0xFF111111),
            titleContentColor = Color.Black,
            textContentColor = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    stringResource(R.string.editor_settings),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SettingsRow(label = stringResource(R.string.evening_reminder)) {
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { viewModel.setReminderEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.Black,
                                uncheckedThumbColor = Color.Black.copy(0.4f),
                                uncheckedTrackColor = Color.Black.copy(0.1f)
                            )
                        )
                    }
                    if (isReminderEnabled) {
                        SettingsRow(label = stringResource(R.string.reminder_time)) {
                            Text(
                                text = reminderTime,
                                modifier = Modifier
                                    .clickable {
                                        val next = if (reminderTime == "21:00") "22:00" else "21:00"
                                        viewModel.setReminderTime(next)
                                    }
                                    .padding(8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black
                            )
                        }
                    }
                    SettingsRow(label = "Čas spánku") {
                        Text(
                            text = sleepTime,
                            modifier = Modifier
                                .clickable {
                                    val next = when (sleepTime) {
                                        "21:00" -> "22:00"
                                        "22:00" -> "23:00"
                                        "23:00" -> "00:00"
                                        else -> "21:00"
                                    }
                                    viewModel.setSleepTime(next)
                                }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }
                    SettingsRow(label = stringResource(R.string.birth_date)) {
                        Text(
                            text = birthDate?.let {
                                SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(it))
                            } ?: stringResource(R.string.set_label),
                            modifier = Modifier
                                .clickable { showDatePicker = true }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                    }
                    if (!isDefaultLauncher) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val roleManager = context.getSystemService(RoleManager::class.java)
                                    if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                                        roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                                    } else Intent(Settings.ACTION_HOME_SETTINGS)
                                } else Intent(Settings.ACTION_HOME_SETTINGS)
                                launcher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.5.dp, Color.Black.copy(0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Text(stringResource(R.string.set_as_default))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setEditorMode(false) }) {
                    Text(stringResource(R.string.close), color = Color.Black.copy(alpha = 0.6f))
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
            },
            containerColor = Color(0xFF111111),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
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
                    placeholder = {
                        Text(stringResource(R.string.what_looking_for), color = Color.Black.copy(alpha = 0.35f))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color.Black.copy(alpha = 0.3f),
                        unfocusedIndicatorColor = Color.Black.copy(alpha = 0.15f)
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
                                .padding(vertical = 13.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                                color = if (isSelected && isSelectionMode) Color.Black else Color.Black.copy(alpha = 0.85f),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelectionMode && isSelected) {
                                Text(
                                    stringResource(R.string.selected),
                                    color = Color.Black.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Black.copy(alpha = 0.75f))
        content()
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GridAppItem(
    app: AppInfo?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(if (app == null) Color.Black.copy(alpha = 0.06f) else Color.Transparent),
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
                    tint = Color.Black.copy(alpha = 0.25f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
