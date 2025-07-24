// File: app/src/main/java/com/wpc/servicesync/utils/Constants.kt
package com.wpc.servicesync.utils

object Constants {
    // SharedPreferences
    const val PREF_NAME = "ServiceSyncPrefs"
    const val EMPLOYEE_DATA = "employee_data"
    const val SESSION_DATA = "session_data"

    // Progress updates
    const val PROGRESS_UPDATE_INTERVAL = 15000L // 15 seconds

    // Meal count limits
    const val MEAL_COUNT_MIN = 1
    const val MEAL_COUNT_MAX = 50

    // Timer formats
    const val TIMER_FORMAT = "mm:ss"
    const val TIMER_FORMAT_LONG = "HH:mm:ss"
}