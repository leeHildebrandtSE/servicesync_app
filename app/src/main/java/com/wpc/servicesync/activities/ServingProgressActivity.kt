package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class ServingProgressActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var servingTimerText: TextView
    private lateinit var sessionSummaryText: TextView
    private lateinit var completeButton: Button
    private lateinit var pauseButton: Button
    private lateinit var reportIssueButton: Button
    private lateinit var servingTimer: TimerUtils

    private var session: ServiceSession? = null
    private var mealsServed = 8
    private var totalMeals = 12
    private var progressHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_serving_progress)

        initViews()
        setupData()
        setupClickListeners()
        startServingTimer()
        startProgressSimulation()
        updateSessionSummary()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        servingTimerText = findViewById(R.id.servingTimerText)
        sessionSummaryText = findViewById(R.id.sessionSummaryText)
        completeButton = findViewById(R.id.completeButton)
        pauseButton = findViewById(R.id.pauseButton)
        reportIssueButton = findViewById(R.id.reportIssueButton)
    }

    private fun setupData() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val sessionJson = sharedPrefs.getString(Constants.SESSION_DATA, null)
        session = Gson().fromJson(sessionJson, ServiceSession::class.java)

        totalMeals = session?.mealCount ?: 12
        mealsServed = (totalMeals * 0.67).toInt() // Start at 67% progress

        updateProgress()
    }

    private fun setupClickListeners() {
        completeButton.setOnClickListener {
            completeService()
        }

        pauseButton.setOnClickListener {
            pauseTimer()
        }

        reportIssueButton.setOnClickListener {
            reportIssue()
        }
    }

    private fun startServingTimer() {
        servingTimer = TimerUtils()
        servingTimer.startTimer { timeString ->
            servingTimerText.text = timeString
        }
    }

    private fun startProgressSimulation() {
        progressHandler = Handler(Looper.getMainLooper())

        val progressRunnable = object : Runnable {
            override fun run() {
                if (mealsServed < totalMeals) {
                    mealsServed++
                    updateProgress()

                    progressHandler?.postDelayed(this, Constants.PROGRESS_UPDATE_INTERVAL)
                } else {
                    completeButton.isEnabled = true
                    completeButton.text = getString(R.string.complete_service)
                }
            }
        }

        progressHandler?.postDelayed(progressRunnable, Constants.PROGRESS_UPDATE_INTERVAL)
    }

    private fun updateProgress() {
        val progress = (mealsServed.toFloat() / totalMeals.toFloat() * 100).toInt()
        progressBar.progress = progress

        progressText.text = getString(R.string.progress_text, mealsServed, totalMeals)

        // Update session
        session = session?.copy(mealsServed = mealsServed)
    }

    private fun updateSessionSummary() {
        val currentSession = session
        if (currentSession == null) {
            sessionSummaryText.text = getString(R.string.no_session_data)
            return
        }

        val kitchenExitTime = currentSession.kitchenExitTime
        val wardArrivalTime = currentSession.wardArrivalTime
        val nurseResponseTime = currentSession.nurseResponseTime
        val nurseAlertTime = currentSession.nurseAlertTime
        val serviceStartTime = currentSession.serviceStartTime

        val travelTime = if (kitchenExitTime != null && wardArrivalTime != null) {
            TimerUtils.formatTimeFromSeconds((wardArrivalTime.time - kitchenExitTime.time) / 1000)
        } else getString(R.string.na)

        val nurseResponseDuration = if (nurseAlertTime != null && nurseResponseTime != null) {
            TimerUtils.formatTimeFromSeconds((nurseResponseTime.time - nurseAlertTime.time) / 1000)
        } else getString(R.string.na)

        val currentRate = if (servingTimer.getElapsedTime() > 0) {
            val ratePerMinute = (mealsServed.toFloat() / (servingTimer.getElapsedTime() / 60000f))
            getString(R.string.current_rate_format, ratePerMinute)
        } else getString(R.string.calculating)

        val summary = getString(
            R.string.session_summary_progress,
            formatTime(kitchenExitTime),
            formatTime(wardArrivalTime),
            travelTime,
            nurseResponseDuration,
            formatTime(serviceStartTime),
            currentRate
        )

        sessionSummaryText.text = summary
    }

    private fun formatTime(date: Date?): String {
        if (date == null) return getString(R.string.na)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    private fun pauseTimer() {
        servingTimer.pauseTimer()
        pauseButton.text = getString(R.string.resume_timer)

        // TODO: Implement resume functionality
    }

    private fun reportIssue() {
        android.widget.Toast.makeText(this, getString(R.string.issue_reporting_coming_soon), android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun completeService() {
        session = session?.copy(
            serviceCompleteTime = Date(),
            mealsServed = totalMeals,
            isCompleted = true
        )

        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            putString(Constants.SESSION_DATA, Gson().toJson(session))
        }

        val intent = Intent(this, ServiceCompleteActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        servingTimer.stopTimer()
        progressHandler?.removeCallbacksAndMessages(null)
    }
}