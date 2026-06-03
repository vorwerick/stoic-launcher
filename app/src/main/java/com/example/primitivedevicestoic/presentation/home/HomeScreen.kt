package com.example.primitivedevicestoic.presentation.home

import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.primitivedevicestoic.R
import com.example.primitivedevicestoic.domain.model.AppInfo
import org.koin.androidx.compose.koinViewModel
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import androidx.core.net.toUri

private fun String.removeDiacritics(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(normalized).replaceAll("")
}

private data class AnswerData(val text: String, val correct: Boolean)
private data class QuestionData(val question: String, val answers: List<AnswerData>)

private val allStoicQuestions = listOf(
    QuestionData(
        "Co je v tvé moci?", listOf(
            AnswerData("Moje myšlenky a činy", true),
            AnswerData("Reakce ostatních", false),
            AnswerData("Výsledky mého úsilí", false)
        )
    ),
    QuestionData(
        "Jak stoici přistupují k překážkám?", listOf(
            AnswerData("Překážka se stává cestou", true),
            AnswerData("Vyhýbají se jim za každou cenu", false),
            AnswerData("Hledají pomoc zvenčí", false)
        )
    ),
    QuestionData(
        "Co je základem stoického myšlení?", listOf(
            AnswerData("Rozlišit, co závisí na nás", true),
            AnswerData("Ovládat všechny situace", false),
            AnswerData("Potlačit všechny emoce", false)
        )
    ),
    QuestionData(
        "Co znamená 'amor fati'?", listOf(
            AnswerData("Láska k osudu", true),
            AnswerData("Strach ze smrti", false),
            AnswerData("Touha po slávě", false)
        )
    ),
    QuestionData(
        "Kdo napsal Hovory k sobě?", listOf(
            AnswerData("Marcus Aurelius", true),
            AnswerData("Seneca", false),
            AnswerData("Epiktétos", false)
        )
    ),
    QuestionData(
        "Co říká 'dichotomie kontroly'?", listOf(
            AnswerData("Řiď jen to, co závisí na tobě", true),
            AnswerData("Kontroluj vše kolem sebe", false),
            AnswerData("Vzdej se veškeré kontroly", false)
        )
    ),
    QuestionData(
        "Co je 'memento mori'?", listOf(
            AnswerData("Připomínka smrtelnosti", true),
            AnswerData("Oslava života", false),
            AnswerData("Druh meditace", false)
        )
    ),
    QuestionData(
        "Jak stoici přistupují k bohatství?", listOf(
            AnswerData("Je lhostejné, záleží na ctnosti", true),
            AnswerData("Je nejvyšším cílem", false),
            AnswerData("Je zdrojem neštěstí", false)
        )
    ),
    QuestionData(
        "Co je 'eudaimonia' pro stoiky?", listOf(
            AnswerData("Dobře žít skrze ctnost", true),
            AnswerData("Maximalizovat požitek", false),
            AnswerData("Vyhnout se utrpení", false)
        )
    ),
    QuestionData(
        "Epiktétos byl původně...", listOf(
            AnswerData("Otrok", true),
            AnswerData("Císař", false),
            AnswerData("Vojevůdce", false)
        )
    ),
    QuestionData(
        "Jak stoici vnímají čas?", listOf(
            AnswerData("Žij přítomným okamžikem", true),
            AnswerData("Plánuj daleko dopředu", false),
            AnswerData("Lituji minulosti", false)
        )
    ),
    QuestionData(
        "Co je 'premeditatio malorum'?", listOf(
            AnswerData("Předvídání a přijetí možných ztrát", true),
            AnswerData("Vyhýbání se negativním myšlenkám", false),
            AnswerData("Modlitba za lepší časy", false)
        )
    ),
    QuestionData(
        "Která ze ctností není stoická?", listOf(
            AnswerData("Popularita", true),
            AnswerData("Moudrost", false),
            AnswerData("Spravedlnost", false)
        )
    ),
    QuestionData(
        "Co Marcus Aurelius říká o ranním vstávání?", listOf(
            AnswerData("Vstaň a konej svou práci", true),
            AnswerData("Odpočinek je klíčem ke ctnosti", false),
            AnswerData("Medituj před vstáváním", false)
        )
    ),
    QuestionData(
        "Co je klíčem ke stoické svobodě?", listOf(
            AnswerData("Nezávislost na vnějších věcech", true),
            AnswerData("Materiální zajištění", false),
            AnswerData("Souhlas ostatních", false)
        )
    ),
)

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
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
    val showEditorTip by viewModel.showEditorTip.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isMottoEnabled by viewModel.isMottoEnabled.collectAsState()
    val listMotto by viewModel.listMotto.collectAsState()
    val currentMottos by viewModel.currentMottos.collectAsState()

    val themeBg = if (isDarkMode) Color.Black else Color.White
    val themeFg = if (isDarkMode) Color.White else Color.Black
    val subtleColor = themeFg.copy(alpha = 0.12f)
    val secondaryText = themeFg.copy(alpha = 0.6f)
    val hintText = themeFg.copy(alpha = 0.4f)

    var showIntentionDialog by remember { mutableStateOf(false) }
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

    val backgroundInteractionSource = remember { MutableInteractionSource() }

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
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Horní řada: Čas/Do spánku a Datum/Dny naživu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Levá strana: Datum a Dny naživu
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = currentDate.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = secondaryText,
                        textAlign = TextAlign.Start
                    )
                    daysAlive?.let {
                        val daysUnit = when {
                            it == 1L -> stringResource(R.string.days_unit_one)
                            it % 10 in 2..4 && (it % 100 < 10 || it % 100 >= 20) -> stringResource(R.string.days_unit_few)
                            else -> stringResource(R.string.days_unit_other)
                        }
                        Text(
                            text = stringResource(R.string.days_alive_label).lowercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            ),
                            color = secondaryText,
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "$it $daysUnit",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = 32.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = themeFg,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.offset(y = (-4).dp)
                        )
                    }
                }

                // Pravá strana: Čas a Do spánku
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 20.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = secondaryText,
                        textAlign = TextAlign.End
                    )
                    timeUntilSleep?.let {
                        Text(
                            text = stringResource(R.string.until_sleep_label).lowercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            ),
                            color = secondaryText,
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = it,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = 32.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = themeFg,
                            textAlign = TextAlign.End,
                            modifier = Modifier.offset(y = (-4).dp)
                        )
                    }
                }
            }

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

            // Záměr
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
                        .clickable { showIntentionDialog = true }
                        .padding(horizontal = 16.dp)
                )
            }

            HorizontalDivider(color = subtleColor, thickness = 0.5.dp)

            // Oblast aplikací (Vertical Carousel nebo Search Results)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (selectedApps.isEmpty()) {
                    if (isMottoEnabled) {
                        Text(
                            text = listMotto.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 3.sp,
                                fontWeight = FontWeight.Light,
                                fontStyle = FontStyle.Italic
                            ),
                            color = themeFg.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.add_app).uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = secondaryText,
                            modifier = Modifier
                                .clickable { isSearchActive = true }
                                .padding(16.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val itemsWithMotto = remember(selectedApps, currentMottos, isMottoEnabled) {
                            val list = mutableListOf<Any>()
                            if (selectedApps.isNotEmpty()) {
                                selectedApps.forEachIndexed { index, app ->
                                    list.add(app)
                                    // Vložit unikátní motto po každé aplikaci, pokud je povoleno
                                    if (isMottoEnabled && index < currentMottos.size) {
                                        list.add(currentMottos[index])
                                    }
                                }
                            }
                            list
                        }

                        itemsWithMotto.forEach { item ->
                            if (item is AppInfo) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .combinedClickable(
                                            onClick = {
                                                try {
                                                    val intent =
                                                        context.packageManager.getLaunchIntentForPackage(
                                                            item.packageName
                                                        )
                                                    if (intent != null) context.startActivity(intent)
                                                } catch (e: Exception) {
                                                }
                                            },
                                            onLongClick = {
                                                viewModel.toggleAppSelection(item)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.label.uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            letterSpacing = 3.sp,
                                            fontWeight = FontWeight.ExtraLight
                                        ),
                                        color = themeFg.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else if (item is String) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            letterSpacing = 3.sp,
                                            fontWeight = FontWeight.ExtraLight
                                        ),
                                        color = themeFg.copy(alpha = 0.7f),
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
            if (!isSearchActive) {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .background(
                            color = themeFg.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Přepínač Dark Mode
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() }
                    ) {
                        Icon(
                            if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle dark mode",
                            tint = secondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }

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
                            tint = secondaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Search ikona
                    IconButton(
                        onClick = {
                            isSearchActive = true
                        }
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
        }
    }

    if (isSearchActive) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = themeBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                                color = hintText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                isSearchActive = false
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close search",
                                    tint = secondaryText
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = themeFg,
                            unfocusedTextColor = themeFg,
                            cursorColor = themeFg,
                            focusedIndicatorColor = subtleColor,
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
                        apps.filter {
                            it.label.removeDiacritics().contains(normalizedQuery, ignoreCase = true)
                        }
                            .sortedBy { it.label.lowercase() }
                    }
                }

                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                    items(
                        items = filteredApps,
                        key = { app -> "${app.packageName}_${app.label}" }
                    ) { app ->
                        val isSelected = selectedPackageNames.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        val intent =
                                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (intent != null) {
                                            context.startActivity(intent)
                                            isSearchActive = false
                                            viewModel.setSearchQuery("")
                                        }
                                    } catch (e: Exception) {
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Light),
                                color = themeFg.copy(alpha = 0.85f),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.toggleAppSelection(app) }) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Close else Icons.Default.Add,
                                    contentDescription = if (isSelected) "Remove from home" else "Add to home",
                                    tint = hintText
                                )
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
                    SettingsRow(label = stringResource(R.string.birth_date), themeFg = themeFg) {
                        Text(
                            text = birthDate?.let {
                                SimpleDateFormat("d. M. yyyy", Locale.getDefault()).format(Date(it))
                            } ?: stringResource(R.string.set_label),
                            modifier = Modifier
                                .clickable { showDatePicker = true }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = themeFg
                        )
                    }
                    SettingsRow(
                        label = stringResource(R.string.sleep_time_label),
                        themeFg = themeFg
                    ) {
                        Text(
                            text = sleepTime,
                            modifier = Modifier
                                .clickable { showTimePicker = true }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = themeFg
                        )
                    }
                    if (!isDefaultLauncher) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    val roleManager =
                                        context.getSystemService(RoleManager::class.java)
                                    if (roleManager != null && roleManager.isRoleAvailable(
                                            RoleManager.ROLE_HOME
                                        )
                                    ) {
                                        roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                                    } else Intent(Settings.ACTION_HOME_SETTINGS)
                                } else Intent(Settings.ACTION_HOME_SETTINGS)
                                launcher.launch(intent)
                            },
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
                        onClick = {
                            val packageName = context.packageName
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "market://details?id=$packageName".toUri()
                                    )
                                )
                            } catch (e: ActivityNotFoundException) {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://play.google.com/store/apps/details?id=$packageName".toUri()
                                    )
                                )
                            }
                        },
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
                    val coffeeUrl = stringResource(R.string.coffee_url)
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, coffeeUrl.toUri())
                            )
                        },
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
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val versionName = packageInfo.versionName ?: "1.0"
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
                    onClick = { viewModel.setEditorMode(false) },
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

    if (showIntentionDialog) {
        AlertDialog(
            onDismissRequest = { showIntentionDialog = false },
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
                    onValueChange = { intentionText = it },
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
                TextButton(onClick = {
                    viewModel.saveIntention(intentionText)
                    showIntentionDialog = false
                }) {
                    Text(
                        stringResource(R.string.set_label),
                        color = themeFg,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showIntentionDialog = false }) {
                    Text(
                        stringResource(android.R.string.cancel),
                        color = secondaryText
                    )
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
                    viewModel.setSleepTime(
                        String.format(
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                    )
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
