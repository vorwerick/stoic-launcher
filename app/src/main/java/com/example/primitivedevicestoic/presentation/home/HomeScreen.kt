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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

private fun String.removeDiacritics(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalized).replaceAll("")
}

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
    val daysAlive by viewModel.daysAlive.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val quote by viewModel.quote.collectAsState()
    val intention by viewModel.intention.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sleepTime by viewModel.sleepTime.collectAsState()
    val timeUntilSleep by viewModel.timeUntilSleep.collectAsState()
    val isDefaultLauncher by viewModel.isDefaultLauncher.collectAsState()

    var showIntentionEdit by remember { mutableStateOf(false) }
    var intentionText by remember { mutableStateOf(intention) }

    LaunchedEffect(intention) { intentionText = intention }

    val selectedPackageNames by remember {
        derivedStateOf { selectedApps.map { it.packageName }.toSet() }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

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
            // Horní řada: Čas/Datum a Statistiky
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Čas a Datum
                Column {
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Light
                        ),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }

                // Statistiky (pod sebou)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    daysAlive?.let {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DNÍ NAŽIVU",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontSize = 12.sp
                                ),
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "$it",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light, fontSize = 16.sp),
                                color = Color.Black
                            )
                        }
                    }
                    timeUntilSleep?.let {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "DO SPÁNKU",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontSize = 12.sp
                                ),
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light, fontSize = 16.sp),
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = subtleWhite, thickness = 0.5.dp)

            // Záměr
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ZÁMĚR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    color = Color.Black.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (showIntentionEdit) {
                    TextField(
                        value = intentionText,
                        onValueChange = { intentionText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                            fontWeight = FontWeight.Light
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
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Light),
                        color = if (intention.isEmpty()) Color.Black.copy(alpha = 0.5f) else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showIntentionEdit = true }
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = subtleWhite, thickness = 0.5.dp)

            // Oblast aplikací (Vertical Carousel nebo Search Results)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f),
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (isSearchActive) {
                        // Search Bar Area uvnitř oblasti aplikací, když je aktivní
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                placeholder = {
                                    Text(
                                        stringResource(R.string.what_looking_for),
                                        color = Color.Black.copy(alpha = 0.4f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                    focusedIndicatorColor = subtleWhite,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                        }
                        
                        val filteredApps by remember(apps, searchQuery) {
                            derivedStateOf {
                                val normalizedQuery = searchQuery.removeDiacritics()
                                apps.filter { it.label.removeDiacritics().contains(normalizedQuery, ignoreCase = true) }
                                    .sortedBy { it.label.lowercase() }
                            }
                        }
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(items = filteredApps, key = { it.packageName }) { app ->
                                val isSelected = selectedPackageNames.contains(app.packageName)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                if (intent != null) {
                                                    context.startActivity(intent)
                                                    isSearchActive = false
                                                    viewModel.setSearchQuery("")
                                                }
                                            } catch (e: Exception) {}
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                                        color = Color.Black.copy(alpha = 0.85f),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { viewModel.toggleAppSelection(app) }) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Close else Icons.Default.Add,
                                            contentDescription = if (isSelected) "Remove from home" else "Add to home",
                                            tint = Color.Black.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    } else if (selectedApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(subtleWhite)
                                    .clickable {
                                        isSearchActive = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.add_app),
                                    tint = Color.Black.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            selectedApps.forEach { app ->
                                val itemHeight = 40.dp
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(itemHeight)
                                        .combinedClickable(
                                            onClick = {
                                                try {
                                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                    if (intent != null) context.startActivity(intent)
                                                } catch (e: Exception) {}
                                            },
                                            onLongClick = {
                                                viewModel.toggleAppSelection(app)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.label.uppercase(),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            letterSpacing = 2.sp,
                                            fontWeight = FontWeight.Light
                                        ),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spodní tlačítka
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikona telefonu
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_CALL_BUTTON)
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Open dialer",
                        tint = Color.Black.copy(alpha = 0.6f)
                    )
                }

                // Search ikona
                IconButton(
                    onClick = {
                        if (isSearchActive) {
                            isSearchActive = false
                            viewModel.setSearchQuery("")
                        } else {
                            isSearchActive = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearchActive) "Close search" else "Search apps",
                        tint = Color.Black.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (isEditorMode) {
        AlertDialog(
            onDismissRequest = { viewModel.setEditorMode(false) },
            containerColor = Color.White,
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
                    SettingsRow(label = stringResource(R.string.sleep_time_label)) {
                        Text(
                            text = sleepTime,
                            modifier = Modifier
                                .clickable { showTimePicker = true }
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

                    Spacer(modifier = Modifier.height(16.dp))
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val versionName = packageInfo.versionName ?: "1.0"
                    Text(
                        text = "v$versionName",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center
                    )
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

    if (showTimePicker) {
        val parts = sleepTime.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 22
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
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
            containerColor = Color.White,
            titleContentColor = Color.Black,
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Black.copy(alpha = 0.85f))
        content()
    }
}
