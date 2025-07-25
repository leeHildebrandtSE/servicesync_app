package com.wpc.servicesync.models

import java.util.Date

/**
 * Data class representing a meal delivery service session
 */
data class ServiceSession(
    // Employee Information
    val employeeId: String = "",
    val hostessName: String = "",
    val shiftSchedule: String = "",

    // Ward Information
    val wardNumber: String = "",
    val wardName: String = "",

    // Meal Information
    val mealType: MealType = MealType.BREAKFAST,
    val mealCount: Int = 12,
    val mealsServed: Int = 0,

    // Service Timestamps
    val kitchenExitTime: Date? = null,
    val wardArrivalTime: Date? = null,
    val dietSheetCaptureTime: Date? = null,
    val nurseAlertTime: Date? = null,
    val nurseResponseTime: Date? = null,
    val serviceStartTime: Date? = null,
    val serviceCompleteTime: Date? = null,

    // Session Metadata
    val sessionId: String = "",
    val sessionStartTime: Date = Date(),
    val comments: String = "",
    val nurseName: String = "",

    // Diet Sheet Documentation
    val dietSheetPhotoPath: String? = null,
    val dietSheetNotes: String = "",
    val dietSheetDocumented: Boolean = false,

    // Service Status
    val isActive: Boolean = true,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false
) {

    /**
     * Calculate travel time from kitchen to ward in milliseconds
     */
    fun getTravelTime(): Long {
        return if (kitchenExitTime != null && wardArrivalTime != null) {
            wardArrivalTime.time - kitchenExitTime.time
        } else {
            0L
        }
    }

    /**
     * Calculate nurse response time in milliseconds
     */
    fun getNurseResponseTime(): Long {
        return if (nurseAlertTime != null && nurseResponseTime != null) {
            nurseResponseTime.time - nurseAlertTime.time
        } else {
            0L
        }
    }

    /**
     * Calculate actual serving time in milliseconds
     */
    fun getServingTime(): Long {
        return if (serviceStartTime != null && serviceCompleteTime != null) {
            serviceCompleteTime.time - serviceStartTime.time
        } else {
            0L
        }
    }

    /**
     * Calculate total elapsed time for the entire service in milliseconds
     */
    fun getElapsedTime(): Long {
        return if (kitchenExitTime != null && serviceCompleteTime != null) {
            serviceCompleteTime.time - kitchenExitTime.time
        } else if (kitchenExitTime != null) {
            // Service in progress
            Date().time - kitchenExitTime.time
        } else {
            0L
        }
    }

    /**
     * Calculate completion rate as percentage
     */
    fun getCompletionRate(): Float {
        return if (mealCount > 0) {
            (mealsServed.toFloat() / mealCount.toFloat()) * 100f
        } else {
            0f
        }
    }

    /**
     * Calculate average serving rate in meals per minute
     */
    fun getAverageServingRate(): Float {
        val servingTimeMinutes = getServingTime() / 60000f // Convert to minutes
        return if (servingTimeMinutes > 0) {
            mealsServed.toFloat() / servingTimeMinutes
        } else {
            0f
        }
    }

    /**
     * Check if service is on schedule based on average performance metrics
     */
    fun isOnSchedule(): Boolean {
        val averageRate = getAverageServingRate()
        return averageRate >= 0.6f // 0.6 meals per minute threshold
    }

    /**
     * Get performance efficiency rating
     */
    fun getEfficiencyRating(): String {
        val completionRate = getCompletionRate()
        val averageRate = getAverageServingRate()

        return when {
            completionRate >= 95f && averageRate >= 0.8f -> "Excellent"
            completionRate >= 85f && averageRate >= 0.6f -> "Good"
            completionRate >= 75f && averageRate >= 0.4f -> "Acceptable"
            else -> "Below Average"
        }
    }

    /**
     * Check if any critical thresholds are exceeded
     */
    fun hasWarnings(): Boolean {
        val travelTime = getTravelTime()
        val nurseResponseTime = getNurseResponseTime()

        return travelTime > 900000L || // > 15 minutes travel
                nurseResponseTime > 300000L || // > 5 minutes nurse response
                getCompletionRate() < 75f // < 75% completion rate
    }

    /**
     * Get list of warning messages
     */
    fun getWarnings(): List<String> {
        val warnings = mutableListOf<String>()

        if (getTravelTime() > 900000L) {
            warnings.add("Travel time exceeded 15 minutes")
        }

        if (getNurseResponseTime() > 300000L) {
            warnings.add("Nurse response time exceeded 5 minutes")
        }

        if (getCompletionRate() < 75f) {
            warnings.add("Completion rate below 75%")
        }

        if (!dietSheetDocumented) {
            warnings.add("Diet sheet not documented")
        }

        return warnings
    }

    /**
     * Generate a summary string for the session
     */
    fun getSummary(): String {
        val duration = com.wpc.servicesync.utils.TimerUtils.formatTime(getElapsedTime())
        val completionRate = String.format(java.util.Locale.getDefault(), "%.1f", getCompletionRate())
        val docStatus = if (dietSheetDocumented) "✓" else "✗"

        return "Ward $wardNumber • $mealsServed/$mealCount ${mealType.displayName} meals • " +
                "$completionRate% complete • Duration: $duration • Doc: $docStatus"
    }

    /**
     * Check if all required documentation is complete
     */
    fun isDocumentationComplete(): Boolean {
        return dietSheetDocumented || dietSheetPhotoPath != null
    }

    /**
     * Get current workflow step
     */
    fun getCurrentStep(): String {
        return when {
            kitchenExitTime == null -> "Kitchen Exit"
            wardArrivalTime == null -> "Ward Arrival"
            !isDocumentationComplete() -> "Diet Sheet Documentation"
            nurseAlertTime == null -> "Nurse Alert"
            nurseResponseTime == null -> "Awaiting Nurse Response"
            serviceStartTime == null -> "Nurse Station"
            !isCompleted -> "Service in Progress"
            else -> "Service Complete"
        }
    }
}