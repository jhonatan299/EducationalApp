package com.frontend.educationalapplication

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

private const val NOTIFICATION_PREFS = "notification_settings"
private const val BLOCK_OTHER_APPS_KEY = "block_other_apps_during_pomodoro"

fun isBlockOtherAppsDuringPomodoroEnabled(context: Context): Boolean =
    context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
        .getBoolean(BLOCK_OTHER_APPS_KEY, false)

fun setBlockOtherAppsDuringPomodoroEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(BLOCK_OTHER_APPS_KEY, enabled)
        .apply()
}

fun hasDoNotDisturbAccess(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
    val manager = context.getSystemService(NotificationManager::class.java)
    return manager?.isNotificationPolicyAccessGranted == true
}

fun openDoNotDisturbAccessSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

private fun setDoNotDisturb(context: Context, enabled: Boolean): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !hasDoNotDisturbAccess(context)) return false
    val manager = context.getSystemService(NotificationManager::class.java) ?: return false
    manager.setInterruptionFilter(
        if (enabled) NotificationManager.INTERRUPTION_FILTER_NONE
        else NotificationManager.INTERRUPTION_FILTER_ALL
    )
    return true
}

@Composable
fun PomodoroDoNotDisturbEffect(
    context: Context,
    enabled: Boolean,
    pomodoroRunning: Boolean
) {
    var activatedByApp by remember { mutableStateOf(false) }

    LaunchedEffect(enabled, pomodoroRunning) {
        if (enabled && pomodoroRunning) {
            activatedByApp = setDoNotDisturb(context, true)
        } else if (activatedByApp) {
            setDoNotDisturb(context, false)
            activatedByApp = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (activatedByApp) setDoNotDisturb(context, false)
        }
    }
}
