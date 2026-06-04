package com.example.primitivedevicestoic.presentation.home

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.os.BatteryManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primitivedevicestoic.domain.model.AppInfo
import com.example.primitivedevicestoic.domain.model.UnlockEvent
import com.example.primitivedevicestoic.domain.repository.UsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.primitivedevicestoic.R
import kotlinx.coroutines.flow.update

data class HomeUiState(
    val unlockEvents: List<UnlockEvent> = emptyList(),
    val screenTimeMinutes: Long = 0L,
    val apps: List<AppInfo> = emptyList(),
    val batteryPercentage: Int = 0,
    val selectedApps: List<AppInfo> = emptyList(),
    val isEditorMode: Boolean = false,
    val isOfflineMode: Boolean = false,
    val birthDate: Long? = null,
    val daysAlive: Long? = null,
    val hoursRemaining: Int = 0,
    val timeSinceLastUse: String = "",
    val quote: String = "",
    val listMotto: String = "",
    val currentMottos: List<String> = emptyList(),
    val intention: String = "",
    val currentTime: String = "",
    val currentDate: String = "",
    val sleepTime: String? = null,
    val timeUntilSleep: String? = null,
    val todayUnlockCount: Int = 0,
    val searchQuery: String = "",
    val isDefaultLauncher: Boolean = false,
    val showEditorTip: Boolean = true,
    val isDarkMode: Boolean = false,
    val isMottoEnabled: Boolean = false,
    val lockTrigger: Int = 0
)

class HomeViewModel(
    private val repository: UsageRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            sleepTime = repository.getSleepTime(),
            isDefaultLauncher = isDefaultLauncher(context),
            showEditorTip = repository.isEditorTipShown(),
            isDarkMode = repository.isDarkMode(),
            isMottoEnabled = repository.isMottoEnabled()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var updateJob: kotlinx.coroutines.Job? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            _uiState.update { it.copy(batteryPercentage = getBatteryLevel()) }
        }
    }

    init {
        fullRefresh()
        loadData()
        updateDefaultLauncherStatus()
        rotateQuote()
        rotateListMotto()
    }

    fun startPeriodicUpdates() {
        try {
            context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (e: Exception) {
            // Ignorujeme, pokud už je registrován nebo jiná chyba
        }
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                // Počkáme do začátku další minuty pro přesnější update času
                val now = System.currentTimeMillis()
                val delayTime = 60000 - (now % 60000)
                kotlinx.coroutines.delay(delayTime)
                refreshStats()
            }
        }
    }

    fun stopPeriodicUpdates() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            // Ignorujeme
        }
        updateJob?.cancel()
        updateJob = null
    }

    fun loadData() {
        _uiState.update { it.copy(
            isOfflineMode = repository.isOfflineMode(),
            birthDate = repository.getBirthDate(),
            intention = repository.getIntention()
        ) }
        calculateDaysAlive()
        calculateHoursRemaining()
        
        viewModelScope.launch {
            repository.getUnlockEvents().collect { events ->
                _uiState.update { it.copy(unlockEvents = events) }
                updateTodayUnlockCount(events)
            }
        }
        viewModelScope.launch {
            repository.getSelectedApps().collect { packageNames ->
                updateSelectedAppsList(packageNames)
            }
        }
    }

    private fun updateSelectedAppsList(packageNames: List<String>) {
        val allApps = _uiState.value.apps.ifEmpty { repository.getInstalledApps() }
        _uiState.update { state ->
            state.copy(
                selectedApps = packageNames.mapNotNull { pkg ->
                    allApps.find { it.packageName == pkg }
                }
            )
        }
    }

    fun toggleAppSelection(app: AppInfo) {
        viewModelScope.launch {
            val current = _uiState.value.selectedApps.map { it.packageName }.toMutableList()
            if (current.contains(app.packageName)) {
                current.remove(app.packageName)
            } else if (current.size < 8) {
                current.add(app.packageName)
            }
            repository.saveSelectedApps(current)
        }
    }

    fun setEditorMode(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                isEditorMode = enabled,
                showEditorTip = if (enabled && state.showEditorTip) false else state.showEditorTip
            )
        }
        if (enabled && _uiState.value.showEditorTip.let { false }) { // Toto je jen pro zachování logiky repository
             // Logika uložení je níže
        }
        if (enabled) {
            viewModelScope.launch {
                repository.setEditorTipShown(false)
            }
        }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isOfflineMode
            repository.setOfflineMode(newValue)
            _uiState.update { it.copy(isOfflineMode = newValue) }
            
            // Pokus o zapnutí režimu letadlo otevřením nastavení
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun toggleDarkMode() {
        val newState = !_uiState.value.isDarkMode
        _uiState.update { it.copy(isDarkMode = newState) }
        viewModelScope.launch {
            repository.setDarkMode(newState)
        }
    }

    fun toggleMottoEnabled() {
        val newState = !_uiState.value.isMottoEnabled
        _uiState.update { it.copy(isMottoEnabled = newState) }
        viewModelScope.launch {
            repository.setMottoEnabled(newState)
        }
    }

    fun setBirthDate(timestamp: Long) {
        viewModelScope.launch {
            repository.saveBirthDate(timestamp)
            _uiState.update { it.copy(birthDate = timestamp) }
            calculateDaysAlive()
        }
    }

    private fun calculateDaysAlive() {
        val birth = _uiState.value.birthDate ?: return
        val birthCalendar = Calendar.getInstance().apply { timeInMillis = birth }
        val todayCalendar = Calendar.getInstance()
        
        // Vynulování času pro přesný výpočet dní
        birthCalendar.set(Calendar.HOUR_OF_DAY, 0)
        birthCalendar.set(Calendar.MINUTE, 0)
        birthCalendar.set(Calendar.SECOND, 0)
        birthCalendar.set(Calendar.MILLISECOND, 0)
        
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        
        val diff = todayCalendar.timeInMillis - birthCalendar.timeInMillis
        val days = (diff / (1000 * 60 * 60 * 24)) + 1 // +1 protože "žiješ svůj první den" v den narození
        _uiState.update { it.copy(daysAlive = if (days > 0) days else null) }
    }

    private fun updateTodayUnlockCount(events: List<UnlockEvent>) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        _uiState.update { it.copy(todayUnlockCount = events.count { event -> event.timestamp >= today }) }
    }

    fun updateDefaultLauncherStatus() {
        _uiState.update { it.copy(isDefaultLauncher = isDefaultLauncher(context)) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun isDefaultLauncher(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val res = context.packageManager.resolveActivity(intent, 0)
        return res?.activityInfo?.packageName == context.packageName
    }

    fun saveIntention(intention: String) {
        viewModelScope.launch {
            repository.saveIntention(intention)
            _uiState.update { it.copy(intention = intention) }
        }
    }

    fun setSleepTime(time: String?) {
        viewModelScope.launch {
            repository.saveSleepTime(time)
            _uiState.update { it.copy(sleepTime = time) }
            calculateTimeUntilSleep()
        }
    }

    fun rotateQuote() {
        val quotes = context.resources.getStringArray(R.array.cynic_quotes).toList().distinct()
        _uiState.update { it.copy(quote = quotes.random()) }
    }

    fun rotateListMotto() {
        val mottos = context.resources.getStringArray(R.array.cynic_advice).toList().distinct()
        
        val usedIndices = repository.getUsedMottoIndices().toMutableSet()
        val allIndices = mottos.indices.toList()
        
        val prefs = context.getSharedPreferences("motto_prefs", Context.MODE_PRIVATE)
        val lastIndex = prefs.getInt("last_motto_index", -1)

        val selectedMottos = mutableListOf<String>()
        val countToSelect = 10 // Potřebujeme dostatek unikátních mott pro seznam
        
        repeat(countToSelect) {
            val availableIndices = allIndices.filter { it !in usedIndices }
            
            val indexToUse = if (availableIndices.isEmpty()) {
                // Všechna motta byla použita, resetujeme
                usedIndices.clear()
                // Vyhneme se poslednímu použitému z minulé sady (v prvním kroku po resetu)
                if (allIndices.size > 1) {
                    allIndices.filter { it != lastIndex }.random()
                } else {
                    allIndices.random()
                }
            } else {
                availableIndices.random()
            }
            
            usedIndices.add(indexToUse)
            selectedMottos.add(mottos[indexToUse])
            
            // Uložíme poslední skutečně vybraný index jako lastIndex pro příště
            prefs.edit().putInt("last_motto_index", indexToUse).apply()
        }
        
        _uiState.update { it.copy(
            currentMottos = selectedMottos,
            listMotto = selectedMottos.firstOrNull() ?: ""
        ) }
        
        viewModelScope.launch {
            repository.saveUsedMottoIndices(usedIndices)
        }
    }

    private fun calculateHoursRemaining() {
        val now = Calendar.getInstance()
        val hours = 23 - now.get(Calendar.HOUR_OF_DAY)
        _uiState.update { it.copy(hoursRemaining = hours) }
    }

    private fun calculateTimeUntilSleep() {
        val sleepStr = _uiState.value.sleepTime ?: run {
            _uiState.update { it.copy(timeUntilSleep = null) }
            return
        }
        val parts = sleepStr.split(":")
        if (parts.size != 2) return
        
        val sleepHour = parts[0].toIntOrNull() ?: return
        val sleepMinute = parts[1].toIntOrNull() ?: return
        
        val now = Calendar.getInstance()
        val sleepTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, sleepHour)
            set(Calendar.MINUTE, sleepMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (sleepTime.before(now)) {
            sleepTime.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val diffMs = sleepTime.timeInMillis - now.timeInMillis
        val diffMinutes = diffMs / (1000 * 60)
        
        if (diffMinutes > 1440) { // Více než 24h by nemělo nastat díky add(DAY, 1)
            _uiState.update { it.copy(timeUntilSleep = null) }
            return
        }

        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        _uiState.update { it.copy(timeUntilSleep = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m") }
    }

    private fun calculateTimeSinceLastUse(events: List<UnlockEvent>) {
        if (events.size < 2) {
            _uiState.update { it.copy(timeSinceLastUse = "0m") }
            return
        }
        // events[0] je aktuální odemčení, events[1] je předchozí
        val lastTimestamp = events[1].timestamp
        val diffMs = System.currentTimeMillis() - lastTimestamp
        val diffMinutes = diffMs / (1000 * 60)
        
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        _uiState.update { it.copy(timeSinceLastUse = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m") }
    }

    fun refreshStats() {
        val events = _uiState.value.unlockEvents
        _uiState.update { it.copy(
            screenTimeMinutes = repository.getScreenTimeMinutes(),
            batteryPercentage = getBatteryLevel()
        ) }
        updateTodayUnlockCount(events)
        updateTimeAndDate()
        calculateDaysAlive()
        calculateHoursRemaining()
        calculateTimeUntilSleep()
        calculateTimeSinceLastUse(events)
    }

    private fun updateTimeAndDate() {
        val now = Calendar.getInstance().time
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        val currentDate = SimpleDateFormat("EEEE d.M.yyyy", Locale.getDefault()).format(now)
        _uiState.update { it.copy(
            currentTime = currentTime,
            currentDate = currentDate
        ) }
    }

    fun fullRefresh() {
        _uiState.update { it.copy(
            lockTrigger = it.lockTrigger + 1,
            apps = repository.getInstalledApps()
        ) }
        refreshStats()
        rotateListMotto()
        updateSelectedAppsList(_uiState.value.selectedApps.map { it.packageName })
    }

    private fun getBatteryLevel(): Int {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
}
