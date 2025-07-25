package com.wpc.servicesync.utils

import android.content.Context
import android.util.Log
import com.wpc.servicesync.models.Employee
import com.wpc.servicesync.models.MealType
import com.wpc.servicesync.models.ServiceSession
import com.google.gson.Gson
import java.util.*

object DebugUtils {
    private const val TAG = "ServiceSync_Debug"

    /**
     * Enable debug mode for testing
     */
    var isDebugMode: Boolean = true // Set to false for production

    /**
     * Log debug information
     */
    fun log(message: String) {
        if (isDebugMode) {
            Log.d(TAG, message)
        }
    }

    /**
     * Generate test data for development
     */
    fun generateTestEmployee(): Employee {
        return Employee(
            employeeId = "H001",
            name = "Sarah Johnson",
            shiftSchedule = "Morning Shift - 7:00 AM",
            hospitalAssignment = "General Hospital - Western Cape",
            wardAssignments = listOf("3A", "3B", "4A", "5A")
        )
    }

    /**
     * Generate test session data
     */
    fun generateTestSession(): ServiceSession {
        val now = Date()
        return ServiceSession(
            sessionId = UUID.randomUUID().toString(),
            employeeId = "H001",
            hostessName = "Sarah Johnson",
            shiftSchedule = "Morning Shift - 7:00 AM",
            wardNumber = "3A",
            wardName = "General Medicine",
            mealType = MealType.BREAKFAST,
            mealCount = 12,
            mealsServed = 0,
            sessionStartTime = now,
            isActive = true
        )
    }

    /**
     * Generate all test QR codes for development
     */
    fun generateAllTestQRCodes(): Map<String, String> {
        val qrCodes = mutableMapOf<String, String>()

        QRUtils.QRType.values().forEach { type ->
            qrCodes[type.displayName] = QRUtils.simulateQRScan(type)
        }

        return qrCodes
    }

    /**
     * Save test data to SharedPreferences for development
     */
    fun saveTestDataToPreferences(context: Context) {
        val sharedPrefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        // Save test employee
        val testEmployee = generateTestEmployee()
        editor.putString(Constants.EMPLOYEE_DATA, Gson().toJson(testEmployee))

        // Save test session
        val testSession = generateTestSession()
        editor.putString(Constants.SESSION_DATA, Gson().toJson(testSession))

        editor.apply()
        log("Test data saved to SharedPreferences")
    }

    /**
     * Clear all app data for testing
     */
    fun clearAllAppData(context: Context) {
        val sharedPrefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        log("All app data cleared")
    }

    /**
     * Validate session data integrity
     */
    fun validateSessionData(session: ServiceSession?): List<String> {
        val issues = mutableListOf<String>()

        if (session == null) {
            issues.add("Session is null")
            return issues
        }

        if (session.employeeId.isBlank()) {
            issues.add("Employee ID is blank")
        }

        if (session.hostessName.isBlank()) {
            issues.add("Hostess name is blank")
        }

        if (session.wardNumber.isBlank()) {
            issues.add("Ward number is blank")
        }

        if (session.mealCount <= 0) {
            issues.add("Invalid meal count: ${session.mealCount}")
        }

        if (session.mealsServed > session.mealCount) {
            issues.add("Meals served (${session.mealsServed}) exceeds meal count (${session.mealCount})")
        }

        return issues
    }

    /**
     * Get session progress summary for debugging
     */
    fun getSessionProgressSummary(session: ServiceSession?): String {
        if (session == null) return "No session data"

        val progress = StringBuilder()
        progress.append("Session Progress Summary:\n")
        progress.append("- Employee: ${session.hostessName} (${session.employeeId})\n")
        progress.append("- Ward: ${session.wardNumber} (${session.wardName})\n")
        progress.append("- Meal: ${session.mealType.displayName} (${session.mealCount} meals)\n")
        progress.append("- Current Step: ${session.getCurrentStep()}\n")

        // Timeline
        progress.append("\nTimeline:\n")
        session.kitchenExitTime?.let {
            progress.append("✓ Kitchen Exit: ${TimerUtils.formatTime(it.time)}\n")
        } ?: progress.append("○ Kitchen Exit: Pending\n")

        session.wardArrivalTime?.let {
            progress.append("✓ Ward Arrival: ${TimerUtils.formatTime(it.time)}\n")
        } ?: progress.append("○ Ward Arrival: Pending\n")

        if (session.isDocumentationComplete()) {
            progress.append("✓ Diet Sheet: Documented\n")
        } else {
            progress.append("○ Diet Sheet: Pending\n")
        }

        session.nurseAlertTime?.let {
            progress.append("✓ Nurse Alert: ${TimerUtils.formatTime(it.time)}\n")
        } ?: progress.append("○ Nurse Alert: Pending\n")

        session.nurseResponseTime?.let {
            progress.append("✓ Nurse Response: ${TimerUtils.formatTime(it.time)}\n")
        } ?: progress.append("○ Nurse Response: Pending\n")

        session.serviceStartTime?.let {
            progress.append("✓ Service Start: ${TimerUtils.formatTime(it.time)}\n")
        } ?: progress.append("○ Service Start: Pending\n")

        session.serviceCompleteTime?.let {
            progress.append("✓ Service Complete: ${TimerUtils.formatTime(it.time)}\n")
        } ?: progress.append("○ Service Complete: Pending\n")

        // Statistics
        if (session.isCompleted) {
            progress.append("\nStatistics:\n")
            progress.append("- Total Duration: ${TimerUtils.formatTime(session.getElapsedTime())}\n")
            progress.append("- Travel Time: ${TimerUtils.formatTime(session.getTravelTime())}\n")
            progress.append("- Nurse Response: ${TimerUtils.formatTime(session.getNurseResponseTime())}\n")
            progress.append("- Serving Time: ${TimerUtils.formatTime(session.getServingTime())}\n")
            progress.append("- Completion Rate: ${String.format("%.1f", session.getCompletionRate())}%\n")
            progress.append("- Efficiency: ${session.getEfficiencyRating()}\n")
        }

        return progress.toString()
    }

    /**
     * Performance simulation for testing
     */
    fun simulateServiceProgress(session: ServiceSession, targetStep: String): ServiceSession {
        val now = Date()
        var updatedSession = session

        when (targetStep.uppercase()) {
            "KITCHEN_EXIT" -> {
                updatedSession = updatedSession.copy(kitchenExitTime = now)
            }
            "WARD_ARRIVAL" -> {
                updatedSession = updatedSession.copy(
                    kitchenExitTime = updatedSession.kitchenExitTime ?: Date(now.time - 300000), // 5 min ago
                    wardArrivalTime = now
                )
            }
            "DIET_SHEET" -> {
                updatedSession = updatedSession.copy(
                    kitchenExitTime = updatedSession.kitchenExitTime ?: Date(now.time - 600000), // 10 min ago
                    wardArrivalTime = updatedSession.wardArrivalTime ?: Date(now.time - 300000), // 5 min ago
                    dietSheetCaptureTime = now,
                    dietSheetDocumented = true
                )
            }
            "NURSE_ALERT" -> {
                updatedSession = updatedSession.copy(
                    kitchenExitTime = updatedSession.kitchenExitTime ?: Date(now.time - 800000),
                    wardArrivalTime = updatedSession.wardArrivalTime ?: Date(now.time - 500000),
                    dietSheetCaptureTime = updatedSession.dietSheetCaptureTime ?: Date(now.time - 300000),
                    dietSheetDocumented = true,
                    nurseAlertTime = now
                )
            }
            "SERVICE_START" -> {
                updatedSession = updatedSession.copy(
                    kitchenExitTime = updatedSession.kitchenExitTime ?: Date(now.time - 1200000),
                    wardArrivalTime = updatedSession.wardArrivalTime ?: Date(now.time - 900000),
                    dietSheetCaptureTime = updatedSession.dietSheetCaptureTime ?: Date(now.time - 600000),
                    dietSheetDocumented = true,
                    nurseAlertTime = updatedSession.nurseAlertTime ?: Date(now.time - 300000),
                    nurseResponseTime = updatedSession.nurseResponseTime ?: Date(now.time - 180000),
                    serviceStartTime = now,
                    nurseName = "Mary Williams"
                )
            }
            "COMPLETE" -> {
                updatedSession = updatedSession.copy(
                    kitchenExitTime = updatedSession.kitchenExitTime ?: Date(now.time - 1800000),
                    wardArrivalTime = updatedSession.wardArrivalTime ?: Date(now.time - 1500000),
                    dietSheetCaptureTime = updatedSession.dietSheetCaptureTime ?: Date(now.time - 1200000),
                    dietSheetDocumented = true,
                    nurseAlertTime = updatedSession.nurseAlertTime ?: Date(now.time - 900000),
                    nurseResponseTime = updatedSession.nurseResponseTime ?: Date(now.time - 720000),
                    serviceStartTime = updatedSession.serviceStartTime ?: Date(now.time - 600000),
                    serviceCompleteTime = now,
                    mealsServed = updatedSession.mealCount,
                    isCompleted = true,
                    nurseName = "Mary Williams"
                )
            }
        }

        return updatedSession
    }
}