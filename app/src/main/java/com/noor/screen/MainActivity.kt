package com.noor.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NoorTheme {
                MainScreenUI(
                    onHideAndStart = { durationMinutes ->
                        hideAppLauncherAndStartTimer(durationMinutes)
                    },
                    onUnhideApp = {
                        unhideAppLauncher()
                    }
                )
            }
        }
    }

    private fun hideAppLauncherAndStartTimer(durationMinutes: Int) {
        val packageManager = packageManager

        // Enable Disguised Launcher Alias ("System Services")
        val disguisedComponent = ComponentName(this, "com.noor.screen.DisguisedActivityLauncher")
        packageManager.setComponentEnabledSetting(
            disguisedComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // Disable Default Launcher Alias
        val mainComponent = ComponentName(this, "com.noor.screen.MainActivityLauncher")
        packageManager.setComponentEnabledSetting(
            mainComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        // Start Timer Service
        TimerManager.startTimer(this, durationMinutes)

        Toast.makeText(
            this,
            "App Hidden! Find 'System Services' in your app drawer.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun unhideAppLauncher() {
        val packageManager = packageManager

        // Enable Default Launcher Alias
        val mainComponent = ComponentName(this, "com.noor.screen.MainActivityLauncher")
        packageManager.setComponentEnabledSetting(
            mainComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // Disable Disguised Launcher Alias
        val disguisedComponent = ComponentName(this, "com.noor.screen.DisguisedActivityLauncher")
        packageManager.setComponentEnabledSetting(
            disguisedComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        Toast.makeText(
            this,
            "App icon restored to 'Noor Screen'.",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun MainScreenUI(
    onHideAndStart: (Int) -> Unit,
    onUnhideApp: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val remainingSeconds by TimerManager.remainingSeconds.collectAsState()
    val isTimeUp by TimerManager.isTimeUpFlow.collectAsState()
    val isTimerRunning by TimerManager.isTimerRunning.collectAsState()

    var selectedMinutes by remember { mutableIntStateOf(30) }

    // Permission States
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    // Periodically update permission statuses when UI is visible
    LaunchedEffect(Unit) {
        while (true) {
            hasOverlayPermission = Settings.canDrawOverlays(context)
            hasAccessibilityPermission = isAccessibilityServiceEnabled(context)
            kotlinx.coroutines.delay(1000L)
        }
    }

    Scaffold(
        containerColor = DeepNavy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Title & Header
            HeaderSection()

            Spacer(modifier = Modifier.height(20.dp))

            // Timer Status Card
            TimerStatusCard(
                remainingSeconds = remainingSeconds,
                isTimeUp = isTimeUp,
                isTimerRunning = isTimerRunning,
                selectedMinutes = selectedMinutes,
                onMinutesChange = { selectedMinutes = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Permissions Checklist
            PermissionsCard(
                hasOverlayPermission = hasOverlayPermission,
                hasAccessibilityPermission = hasAccessibilityPermission,
                onRequestOverlay = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                },
                onRequestAccessibility = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "App Controls",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Hide & Start Button
                    Button(
                        onClick = { onHideAndStart(selectedMinutes) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = DeepNavy
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("HIDE APP & START TIMER", fontWeight = FontWeight.Bold)
                    }

                    // Unhide Button
                    OutlinedButton(
                        onClick = onUnhideApp,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UNHIDE APP / RESTORE ICON", fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Test Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                TimerManager.setTimeUpState(true)
                                Toast.makeText(context, "Simulated Time's Up! Open TikTok or Facebook Lite to test.", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simulate Time's Up", fontSize = 11.sp, textAlign = TextAlign.Center)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(context, LockScreenActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test Lock Screen", fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GoldAccent.copy(alpha = 0.15f))
                .border(2.dp, GoldAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockClock,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "NOOR SCREEN",
            color = GoldLight,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Text(
            text = "Mindful Screen-Time Restriction with Quran",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TimerStatusCard(
    remainingSeconds: Long,
    isTimeUp: Boolean,
    isTimerRunning: Boolean,
    selectedMinutes: Int,
    onMinutesChange: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GoldAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Countdown Timer",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                // Status Badge
                val (statusText, statusColor) = when {
                    isTimeUp -> "TIME'S UP" to ErrorRed
                    isTimerRunning -> "ACTIVE" to SuccessGreen
                    else -> "READY" to GoldAccent
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timer Clock Display
            Text(
                text = formatSeconds(remainingSeconds),
                color = TextOffWhite,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Duration Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Timer Duration:", color = TextMuted, fontSize = 13.sp)
                    Text(text = "$selectedMinutes Minutes", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Slider(
                    value = selectedMinutes.toFloat(),
                    onValueChange = { onMinutesChange(it.toInt()) },
                    valueRange = 1f..120f,
                    steps = 118,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldAccent,
                        activeTrackColor = GoldAccent,
                        inactiveTrackColor = DeepNavy
                    )
                )

                // Quick Preset Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(15, 30, 45, 60, 90).forEach { mins ->
                        FilterChip(
                            selected = selectedMinutes == mins,
                            onClick = { onMinutesChange(mins) },
                            label = { Text("${mins}m", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = DeepNavy,
                                containerColor = DeepNavy,
                                labelColor = TextOffWhite
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionsCard(
    hasOverlayPermission: Boolean,
    hasAccessibilityPermission: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GoldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Required Permissions",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Overlay Item
            PermissionRow(
                title = "System Overlay Permission",
                subtitle = "Required to display Quran lock screen over Facebook Lite & TikTok",
                isGranted = hasOverlayPermission,
                onRequest = onRequestOverlay
            )

            // Accessibility Item
            PermissionRow(
                title = "Accessibility Service",
                subtitle = "Required to detect when social media apps are launched",
                isGranted = hasAccessibilityPermission,
                onRequest = onRequestAccessibility
            )
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextOffWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = SuccessGreen,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = DeepNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatSeconds(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        String.format("%02d:%02d:%02d", h, m, s)
    } else {
        String.format("%02d:%02d", m, s)
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedService = "${context.packageName}/${AppWatcherService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val colonSplitter = TextUtils.SimpleStringSplitter(':')
    colonSplitter.setString(enabledServices)

    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedService, ignoreCase = true)) {
            return true
        }
    }
    return false
}
