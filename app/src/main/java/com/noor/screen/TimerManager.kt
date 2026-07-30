package com.noor.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerManager {

    var timerDurationMinutes: Int = 30
        private set

    private val _remainingSeconds = MutableStateFlow(1800L) // Default 30 mins (1800 seconds)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    private val _isTimeUp = MutableStateFlow(false)
    val isTimeUpFlow: StateFlow<Boolean> = _isTimeUp.asStateFlow()

    var isTimeUp: Boolean
        get() = _isTimeUp.value
        set(value) {
            _isTimeUp.value = value
        }

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    fun startTimer(context: Context, durationMinutes: Int) {
        this.timerDurationMinutes = durationMinutes
        val totalSecs = durationMinutes * 60L
        _remainingSeconds.value = totalSecs
        _isTimeUp.value = false
        _isTimerRunning.value = true

        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION_SECONDS, totalSecs)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun pauseTimer(context: Context) {
        _isTimerRunning.value = false
        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        context.startService(serviceIntent)
    }

    fun resetTimer(context: Context) {
        val totalSecs = timerDurationMinutes * 60L
        _remainingSeconds.value = totalSecs
        _isTimeUp.value = false
        _isTimerRunning.value = true

        val serviceIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET
            putExtra(TimerService.EXTRA_DURATION_SECONDS, totalSecs)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun setTimeUpState(timeUp: Boolean) {
        _isTimeUp.value = timeUp
        if (timeUp) {
            _remainingSeconds.value = 0L
            _isTimerRunning.value = false
        }
    }

    fun updateRemainingSeconds(seconds: Long) {
        _remainingSeconds.value = seconds
        if (seconds <= 0L) {
            _isTimeUp.value = true
            _isTimerRunning.value = false
        }
    }

    fun setTimerRunningState(running: Boolean) {
        _isTimerRunning.value = running
    }
}
