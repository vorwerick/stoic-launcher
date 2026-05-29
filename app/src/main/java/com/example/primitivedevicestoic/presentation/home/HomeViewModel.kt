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
import java.util.Calendar

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

    private val _isReminderEnabled = MutableStateFlow(false)
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    private val _reminderTime = MutableStateFlow("21:00")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

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

    private val _intention = MutableStateFlow("")
    val intention: StateFlow<String> = _intention.asStateFlow()

    private val _sleepTime = MutableStateFlow("23:00")
    val sleepTime: StateFlow<String> = _sleepTime.asStateFlow()

    private val _timeUntilSleep = MutableStateFlow("")
    val timeUntilSleep: StateFlow<String> = _timeUntilSleep.asStateFlow()

    private val _todayUnlockCount = MutableStateFlow(0)
    val todayUnlockCount: StateFlow<Int> = _todayUnlockCount.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDefaultLauncher = MutableStateFlow(isDefaultLauncher(context))
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    private val _lockTrigger = MutableStateFlow(0)
    val lockTrigger: StateFlow<Int> = _lockTrigger.asStateFlow()

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
        _isReminderEnabled.value = repository.getReminderEnabled()
        _reminderTime.value = repository.getReminderTime()
        _isOfflineMode.value = repository.isOfflineMode()
        _birthDate.value = repository.getBirthDate()
        _intention.value = repository.getIntention()
        _sleepTime.value = repository.getSleepTime()
        calculateDaysAlive()
        calculateHoursRemaining()
        calculateTimeUntilSleep()
        
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
            } else if (current.size < 10) {
                current.add(app.packageName)
            }
            repository.saveSelectedApps(current)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.saveReminderEnabled(enabled)
            _isReminderEnabled.value = enabled
            com.example.primitivedevicestoic.data.local.ReminderReceiver().scheduleReminder(context)
        }
    }

    fun setReminderTime(time: String) {
        viewModelScope.launch {
            repository.saveReminderTime(time)
            _reminderTime.value = time
            com.example.primitivedevicestoic.data.local.ReminderReceiver().scheduleReminder(context)
        }
    }

    fun setEditorMode(enabled: Boolean) {
        _isEditorMode.value = enabled
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
            // Zde by bylo dobré také přeplánovat notifikaci retrospektivy
            com.example.primitivedevicestoic.data.local.ReminderReceiver().scheduleReminder(context)
        }
    }

    private fun rotateQuote() {
        val quotes = listOf(
            "Máš kontrolu jen nad svým jednáním.",
            "Nepotřebuješ reagovat okamžitě.",
            "Vše je pomíjivé.",
            "Štěstí tvého života závisí na kvalitě tvých myšlenek.",
            "Nehledej, aby se věci děly tak, jak si přeješ, ale přej si, aby se děly tak, jak se dějí.",
            "Překážka v konání posouvá konání. To, co stojí v cestě, se stává cestou.",
            "Čas je jako řeka tvořená událostmi, silný proud."
        )
        _quote.value = quotes.random()
    }

    private fun calculateHoursRemaining() {
        val now = Calendar.getInstance()
        val hours = 23 - now.get(Calendar.HOUR_OF_DAY)
        _hoursRemaining.value = hours
    }

    private fun calculateTimeUntilSleep() {
        val sleepTimeStr = _sleepTime.value
        val parts = sleepTimeStr.split(":")
        val sleepHour = parts.getOrNull(0)?.toIntOrNull() ?: 23
        val sleepMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val sleepTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, sleepHour)
            set(Calendar.MINUTE, sleepMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (now.after(sleepTime)) {
            // Pokud už je po čase spánku dnes, počítáme do zítřejšího času spánku? 
            // Nebo prostě ukážeme 0? Zadání říká "do konce dne zbývá X hodin a minut do spánku".
            // Pokud už spím, tak asi 0.
            _timeUntilSleep.value = "0h 0m"
            return
        }

        val diffMs = sleepTime.timeInMillis - now.timeInMillis
        val diffMinutesTotal = diffMs / (1000 * 60)
        val hours = diffMinutesTotal / 60
        val minutes = diffMinutesTotal % 60

        _timeUntilSleep.value = "${hours}h ${minutes}m"
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
        calculateDaysAlive()
        calculateHoursRemaining()
        calculateTimeUntilSleep()
        calculateTimeSinceLastUse(_unlockEvents.value)
    }

    fun fullRefresh() {
        _lockTrigger.value++
        refreshStats()
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
