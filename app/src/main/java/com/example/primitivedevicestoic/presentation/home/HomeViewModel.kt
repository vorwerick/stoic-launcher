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

class HomeViewModel(
    private val repository: UsageRepository,
    private val context: Context
) : ViewModel() {

    private val _unlockEvents = MutableStateFlow<List<UnlockEvent>>(emptyList())
    val unlockEvents: StateFlow<List<UnlockEvent>> = _unlockEvents.asStateFlow()

    private val _screenTimeMinutes = MutableStateFlow(0L)
    val screenTimeMinutes: StateFlow<Long> = _screenTimeMinutes.asStateFlow()

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(0)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _selectedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val selectedApps: StateFlow<List<AppInfo>> = _selectedApps.asStateFlow()

    private val _isEditorMode = MutableStateFlow(false)
    val isEditorMode: StateFlow<Boolean> = _isEditorMode.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _birthDate = MutableStateFlow<Long?>(null)
    val birthDate: StateFlow<Long?> = _birthDate.asStateFlow()

    private val _daysAlive = MutableStateFlow<Long?>(null)
    val daysAlive: StateFlow<Long?> = _daysAlive.asStateFlow()

    private val _hoursRemaining = MutableStateFlow(0)
    val hoursRemaining: StateFlow<Int> = _hoursRemaining.asStateFlow()

    private val _timeSinceLastUse = MutableStateFlow<String>("")
    val timeSinceLastUse: StateFlow<String> = _timeSinceLastUse.asStateFlow()

    private val _quote = MutableStateFlow("")
    val quote: StateFlow<String> = _quote.asStateFlow()

    private val _listMotto = MutableStateFlow("")
    val listMotto: StateFlow<String> = _listMotto.asStateFlow()

    private val _currentMottos = MutableStateFlow<List<String>>(emptyList())
    val currentMottos: StateFlow<List<String>> = _currentMottos.asStateFlow()

    private val _intention = MutableStateFlow("")
    val intention: StateFlow<String> = _intention.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _sleepTime = MutableStateFlow(repository.getSleepTime())
    val sleepTime: StateFlow<String> = _sleepTime.asStateFlow()

    private val _timeUntilSleep = MutableStateFlow<String?>(null)
    val timeUntilSleep: StateFlow<String?> = _timeUntilSleep.asStateFlow()

    private val _todayUnlockCount = MutableStateFlow(0)
    val todayUnlockCount: StateFlow<Int> = _todayUnlockCount.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(isDefaultLauncher(context))
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val _showEditorTip = MutableStateFlow(repository.isEditorTipShown())
    val showEditorTip: StateFlow<Boolean> = _showEditorTip.asStateFlow()

    private val _isDarkMode = MutableStateFlow(repository.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _lockTrigger = MutableStateFlow(0)
    val lockTrigger: StateFlow<Int> = _lockTrigger.asStateFlow()

    private val _isMottoEnabled = MutableStateFlow(repository.isMottoEnabled())
    val isMottoEnabled: StateFlow<Boolean> = _isMottoEnabled.asStateFlow()

    private var updateJob: kotlinx.coroutines.Job? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            _batteryPercentage.value = getBatteryLevel()
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
        _isOfflineMode.value = repository.isOfflineMode()
        _birthDate.value = repository.getBirthDate()
        _intention.value = repository.getIntention()
        calculateDaysAlive()
        calculateHoursRemaining()
        
        viewModelScope.launch {
            repository.getUnlockEvents().collect {
                _unlockEvents.value = it
                updateTodayUnlockCount(it)
            }
        }
        viewModelScope.launch {
            repository.getSelectedApps().collect { packageNames ->
                updateSelectedAppsList(packageNames)
            }
        }
    }

    private fun updateSelectedAppsList(packageNames: List<String>) {
        val allApps = _apps.value.ifEmpty { repository.getInstalledApps() }
        _selectedApps.value = packageNames.mapNotNull { pkg ->
            allApps.find { it.packageName == pkg }
        }
    }

    fun toggleAppSelection(app: AppInfo) {
        viewModelScope.launch {
            val current = _selectedApps.value.map { it.packageName }.toMutableList()
            if (current.contains(app.packageName)) {
                current.remove(app.packageName)
            } else if (current.size < 8) {
                current.add(app.packageName)
            }
            repository.saveSelectedApps(current)
        }
    }

    fun setEditorMode(enabled: Boolean) {
        _isEditorMode.value = enabled
        if (enabled && _showEditorTip.value) {
            _showEditorTip.value = false
            viewModelScope.launch {
                repository.setEditorTipShown(false)
            }
        }
    }

    fun toggleOfflineMode() {
        viewModelScope.launch {
            val newValue = !_isOfflineMode.value
            repository.setOfflineMode(newValue)
            _isOfflineMode.value = newValue
            
            // Pokus o zapnutí režimu letadlo otevřením nastavení
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun toggleDarkMode() {
        val newState = !_isDarkMode.value
        _isDarkMode.value = newState
        viewModelScope.launch {
            repository.setDarkMode(newState)
        }
    }

    fun toggleMottoEnabled() {
        val newState = !_isMottoEnabled.value
        _isMottoEnabled.value = newState
        viewModelScope.launch {
            repository.setMottoEnabled(newState)
        }
    }

    fun setBirthDate(timestamp: Long) {
        viewModelScope.launch {
            repository.saveBirthDate(timestamp)
            _birthDate.value = timestamp
            calculateDaysAlive()
        }
    }

    private fun calculateDaysAlive() {
        val birth = _birthDate.value ?: return
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
        _daysAlive.value = if (days > 0) days else null
    }

    private fun updateTodayUnlockCount(events: List<UnlockEvent>) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        _todayUnlockCount.value = events.count { it.timestamp >= today }
    }

    fun updateDefaultLauncherStatus() {
        _isDefaultLauncher.value = isDefaultLauncher(context)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
            _intention.value = intention
        }
    }

    fun setSleepTime(time: String) {
        viewModelScope.launch {
            repository.saveSleepTime(time)
            _sleepTime.value = time
            calculateTimeUntilSleep()
        }
    }

    fun rotateQuote() {
        val quotes = context.resources.getStringArray(R.array.stoic_quotes).toList().distinct()
        _quote.value = quotes.random()
    }

    fun rotateListMotto() {
        val mottos = context.resources.getStringArray(R.array.stoic_advice).toList().distinct()
        
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
        
        _currentMottos.value = selectedMottos
        _listMotto.value = selectedMottos.firstOrNull() ?: ""
        
        viewModelScope.launch {
            repository.saveUsedMottoIndices(usedIndices)
        }
    }

    private fun calculateHoursRemaining() {
        val now = Calendar.getInstance()
        val hours = 23 - now.get(Calendar.HOUR_OF_DAY)
        _hoursRemaining.value = hours
    }

    private fun calculateTimeUntilSleep() {
        val sleepStr = _sleepTime.value
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
             _timeUntilSleep.value = null
             return
        }

        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        _timeUntilSleep.value = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    private fun calculateTimeSinceLastUse(events: List<UnlockEvent>) {
        if (events.size < 2) {
            _timeSinceLastUse.value = "0m"
            return
        }
        // events[0] je aktuální odemčení, events[1] je předchozí
        val lastTimestamp = events[1].timestamp
        val diffMs = System.currentTimeMillis() - lastTimestamp
        val diffMinutes = diffMs / (1000 * 60)
        
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        _timeSinceLastUse.value = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    fun refreshStats() {
        _screenTimeMinutes.value = repository.getScreenTimeMinutes()
        _batteryPercentage.value = getBatteryLevel()
        updateTodayUnlockCount(_unlockEvents.value)
        updateTimeAndDate()
        calculateDaysAlive()
        calculateHoursRemaining()
        calculateTimeUntilSleep()
        calculateTimeSinceLastUse(_unlockEvents.value)
    }

    private fun updateTimeAndDate() {
        val now = Calendar.getInstance().time
        _currentTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
        val dateStr = SimpleDateFormat("EEEE d.M.yyyy", Locale.getDefault()).format(now)
        _currentDate.value = dateStr
    }

    fun fullRefresh() {
        _lockTrigger.value++
        refreshStats()
        rotateListMotto()
        _apps.value = repository.getInstalledApps()
        updateSelectedAppsList(_selectedApps.value.map { it.packageName })
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
