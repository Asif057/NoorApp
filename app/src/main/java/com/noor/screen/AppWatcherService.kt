package com.noor.screen

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppWatcherService : AccessibilityService() {

    private val blockedPackages = setOf(
        "com.facebook.lite",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            Log.d("AppWatcherService", "Window changed: $packageName, isTimeUp=${TimerManager.isTimeUp}")

            if (blockedPackages.contains(packageName) && TimerManager.isTimeUp) {
                launchLockScreen(packageName)
            }
        }
    }

    private fun launchLockScreen(blockedPackage: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("BLOCKED_PACKAGE", blockedPackage)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Required method override for AccessibilityService
    }
}
