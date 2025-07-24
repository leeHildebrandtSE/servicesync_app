package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.wpc.servicesync.R
import com.wpc.servicesync.models.ServiceSession
import com.wpc.servicesync.utils.Constants
import com.wpc.servicesync.utils.TimerUtils
import com.google.gson.Gson
import java.util.Date
import java.util.Locale

class ServiceCompleteActivity : AppCompatActivity() {

    private lateinit var completionStatusText: TextView
    private lateinit var finalProgressBar: ProgressBar
    private lateinit var finalProgressText: TextView
    private lateinit var totalDurationText: TextView
    private lateinit var performanceReportText: TextView
    private lateinit var nextDeliveryButton: Button
    private lateinit var fullReportButton: Button
    private lateinit var returnKitchenButton: Button

    private var session: ServiceSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_complete)

        initViews()
        setupData()
        setupClickListeners()
        displayFinalReport()
    }

    private fun initViews() {
        completionStatusText = findViewById(R.id.completionStatusText)
        finalProgressBar = findViewById(R.id.finalProgressBar)
        finalProgressText = findViewById(R.id.finalProgressText)
        totalDurationText = findViewById(R.id.totalDurationText)
        performanceReportText = findViewById(R.id.performanceReportText)
        nextDeliveryButton = findViewById(R.id.nextDeliveryButton)
        fullReportButton = findViewById(R.id.fullReportButton)
        returnKitchenButton = findViewById(R.id.returnKitchenButton)
    }

    private fun setupData() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val sessionJson = sharedPrefs.getString(Constants.SESSION_DATA, null)
        session = Gson().fromJson(sessionJson, ServiceSession::class.java)
    }

    private fun setupClickListeners() {
        nextDeliveryButton.setOnClickListener {
            startNextDelivery()
        }

        fullReportButton.setOnClickListener {
            viewFullReport()
        }

        returnKitchenButton.setOnClickListener {
            returnToKitchen()
        }
    }

    private fun displayFinalReport() {
        val currentSession = session ?: return

        val mealCount = currentSession.mealCount
        val mealsServed = currentSession.mealsServed
        val completionRate = currentSession.getCompletionRate()

        // Get meal type with proper fallback
        val mealType = currentSession.mealType.displayName.lowercase(Locale.getDefault())

        // Completion Status
        completionStatusText.text = getString(
            R.string.service_completed_successfully,
            mealsServed,
            mealType,
            completionRate
        )

        // Final Progress
        finalProgressBar.progress = completionRate.toInt()
        finalProgressText.text = getString(R.string.progress_text, mealsServed, mealCount)

        // Total Duration
        val totalDuration = currentSession.getElapsedTime()
        totalDurationText.text = TimerUtils.formatTime(totalDuration)

        // Performance Report
        generatePerformanceReport()
    }

    private fun generatePerformanceReport() {
        val currentSession = session ?: return

        // Get all timestamp data
        val kitchenExitTime = currentSession.kitchenExitTime
        val wardArrivalTime = currentSession.wardArrivalTime
        val nurseAlertTime = currentSession.nurseAlertTime
        val nurseResponseTime = currentSession.nurseResponseTime
        val serviceStartTime = currentSession.serviceStartTime
        val serviceCompleteTime = currentSession.serviceCompleteTime

        // Calculate durations
        val travelTime = currentSession.getTravelTime()
        val nurseResponseDuration = currentSession.getNurseResponseTime()
        val servingTime = currentSession.getServingTime()
        val totalDuration = currentSession.getElapsedTime()

        // Calculate performance metrics
        val mealsServed = currentSession.mealsServed
        val averageRate = if (servingTime > 0) {
            mealsServed.toFloat() / (servingTime / 60000f) // Convert to minutes
        } else 0f

        // Determine efficiency rating
        val efficiency = if (averageRate > 0.6f) {
            getString(R.string.efficiency_above_average)
        } else {
            getString(R.string.efficiency_below_average)
        }

        // Get meal type for display
        val mealType = currentSession.mealType.displayName.lowercase(Locale.getDefault())

        // Generate the complete performance report using String.format for better control
        val reportText = String.format(
            Locale.getDefault(),
            "Kitchen Exit: %s\nWard Arrival: %s (%s)\nNurse Alert: %s\nNurse Response: %s (%s)\nService Start: %s\nService Complete: %s\n\nTotal Duration: %s\nMeals Served: %d %s meals\nAverage Rate: %.2f meals/minute\nEfficiency: %s",
            formatTime(kitchenExitTime),
            formatTime(wardArrivalTime),
            TimerUtils.formatTime(travelTime),
            formatTime(nurseAlertTime),
            formatTime(nurseResponseTime),
            TimerUtils.formatTime(nurseResponseDuration),
            formatTime(serviceStartTime),
            formatTime(serviceCompleteTime),
            TimerUtils.formatTime(totalDuration),
            mealsServed,
            mealType,
            averageRate,
            efficiency
        )

        performanceReportText.text = reportText
    }

    private fun formatTime(date: Date?): String {
        return if (date == null) {
            getString(R.string.na)
        } else {
            val format = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            format.format(date)
        }
    }

    private fun startNextDelivery() {
        // Clear session data
        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            remove(Constants.SESSION_DATA)
        }

        // Navigate back to Kitchen Exit
        val intent = Intent(this, KitchenExitActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun viewFullReport() {
        android.widget.Toast.makeText(
            this,
            getString(R.string.full_report_coming_soon),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun returnToKitchen() {
        // Navigate back to Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}