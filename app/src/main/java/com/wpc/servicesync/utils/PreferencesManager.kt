package com.wpc.servicesync.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.wpc.servicesync.models.Employee
import com.wpc.servicesync.models.ServiceSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PreferencesManager {

    private const val PREF_NAME = "ServiceSyncPrefs"

    // Keys
    private const val KEY_EMPLOYEE_DATA = "employee_data"
    private const val KEY_SESSION_DATA = "session_data"
    private const val KEY_LAST_WARD = "last_ward"
    private const val KEY_DEFAULT_MEAL_COUNT = "default_meal_count"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_CAMERA_QUALITY = "camera_quality"
    private const val KEY_AUTO_ADVANCE = "auto_advance"
    private const val KEY_SESSION_HISTORY = "session_history"
    private const val KEY_APP_VERSION = "app_version"
    private const val KEY_LAST_SYNC = "last_sync"
    private const val KEY_DEBUG_MODE = "debug_mode"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_VIBRATION_ENABLED = "vibration_enabled"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Employee Data
    fun saveEmployee(context: Context, employee: Employee) {
        getPreferences(context).edit {
            putString(KEY_EMPLOYEE_DATA, Gson().toJson(employee))
        }
    }

    fun getEmployee(context: Context): Employee? {
        val json = getPreferences(context).getString(KEY_EMPLOYEE_DATA, null)
        return if (json != null) {
            try {
                Gson().fromJson(json, Employee::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    // Session Data
    fun saveSession(context: Context, session: ServiceSession) {
        getPreferences(context).edit {
            putString(KEY_SESSION_DATA, Gson().toJson(session))
        }
    }

    fun getSession(context: Context): ServiceSession? {
        val json = getPreferences(context).getString(KEY_SESSION_DATA, null)
        return if (json != null) {
            try {
                Gson().fromJson(json, ServiceSession::class.java)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun clearSession(context: Context) {
        getPreferences(context).edit {
            remove(KEY_SESSION_DATA)
        }
    }

    // Session History
    fun saveSessionToHistory(context: Context, session: ServiceSession) {
        val history = getSessionHistory(context).toMutableList()
        history.add(0, session) // Add to beginning

        // Keep only last 50 sessions
        if (history.size > 50) {
            history.removeAt(history.size - 1)
        }

        getPreferences(context).edit {
            putString(KEY_SESSION_HISTORY, Gson().toJson(history))
        }
    }

    fun getSessionHistory(context: Context): List<ServiceSession> {
        val json = getPreferences(context).getString(KEY_SESSION_HISTORY, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<ServiceSession>>() {}.type
                Gson().fromJson<List<ServiceSession>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    // App Settings
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit {
            putBoolean(KEY_SOUND_ENABLED, enabled)
        }
    }

    fun isSoundEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit {
            putBoolean(KEY_VIBRATION_ENABLED, enabled)
        }
    }

    fun isVibrationEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setAutoAdvance(context: Context, enabled: Boolean) {
        getPreferences(context).edit {
            putBoolean(KEY_AUTO_ADVANCE, enabled)
        }
    }

    fun isAutoAdvanceEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_AUTO_ADVANCE, false)
    }

    // User Preferences
    fun setLastWard(context: Context, ward: String) {
        getPreferences(context).edit {
            putString(KEY_LAST_WARD, ward)
        }
    }

    fun getLastWard(context: Context): String {
        return getPreferences(context).getString(KEY_LAST_WARD, "3A") ?: "3A"
    }

    fun setDefaultMealCount(context: Context, count: Int) {
        getPreferences(context).edit {
            putInt(KEY_DEFAULT_MEAL_COUNT, count)
        }
    }

    fun getDefaultMealCount(context: Context): Int {
        return getPreferences(context).getInt(KEY_DEFAULT_MEAL_COUNT, 12)
    }

    fun setCameraQuality(context: Context, quality: String) {
        getPreferences(context).edit {
            putString(KEY_CAMERA_QUALITY, quality)
        }
    }

    fun getCameraQuality(context: Context): String {
        return getPreferences(context).getString(KEY_CAMERA_QUALITY, "HIGH") ?: "HIGH"
    }

    // App Metadata
    fun setAppVersion(context: Context, version: String) {
        getPreferences(context).edit {
            putString(KEY_APP_VERSION, version)
        }
    }

    fun getAppVersion(context: Context): String? {
        return getPreferences(context).getString(KEY_APP_VERSION, null)
    }

    fun setLastSync(context: Context, timestamp: Long) {
        getPreferences(context).edit {
            putLong(KEY_LAST_SYNC, timestamp)
        }
    }

    fun getLastSync(context: Context): Long {
        return getPreferences(context).getLong(KEY_LAST_SYNC, 0)
    }

    // Debug Settings
    fun setDebugMode(context: Context, enabled: Boolean) {
        getPreferences(context).edit {
            putBoolean(KEY_DEBUG_MODE, enabled)
        }
    }

    fun isDebugMode(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_DEBUG_MODE, false)
    }

    // Utility Methods
    fun clearAllData(context: Context) {
        getPreferences(context).edit().clear().apply()
    }

    fun exportPreferences(context: Context): Map<String, Any?> {
        return getPreferences(context).all
    }

    fun getPreferencesSize(context: Context): Int {
        return getPreferences(context).all.size
    }

    // Performance tracking
    fun getSessionStats(context: Context): SessionStats {
        val history = getSessionHistory(context)
        if (history.isEmpty()) {
            return SessionStats()
        }

        val totalSessions = history.size
        val completedSessions = history.count { it.isCompleted }
        val averageDuration = history.filter { it.isCompleted }
            .map { it.getElapsedTime() }
            .average()
            .toLong()

        val averageCompletionRate = history.filter { it.isCompleted }
            .map { it.getCompletionRate() }
            .average()
            .toFloat()

        val averageServingRate = history.filter { it.isCompleted }
            .map { it.getAverageServingRate() }
            .filter { it > 0 }
            .average()
            .toFloat()

        return SessionStats(
            totalSessions = totalSessions,
            completedSessions = completedSessions,
            averageDuration = averageDuration,
            averageCompletionRate = averageCompletionRate,
            averageServingRate = averageServingRate
        )
    }

    data class SessionStats(
        val totalSessions: Int = 0,
        val completedSessions: Int = 0,
        val averageDuration: Long = 0,
        val averageCompletionRate: Float = 0f,
        val averageServingRate: Float = 0f
    ) {
        fun getCompletionPercentage(): Float {
            return if (totalSessions > 0) {
                (completedSessions.toFloat() / totalSessions.toFloat()) * 100f
            } else 0f
        }

        fun getFormattedAverageDuration(): String {
            return TimerUtils.formatTime(averageDuration)
        }
    }
}