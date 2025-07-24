package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.wpc.servicesync.R
import com.wpc.servicesync.models.ServiceSession
import com.wpc.servicesync.utils.Constants
import com.wpc.servicesync.utils.TimerUtils
import com.google.gson.Gson
import java.util.Date

class NurseAlertActivity : AppCompatActivity() {

    private lateinit var alertStatusText: TextView
    private lateinit var buzzerScreenText: TextView
    private lateinit var nurseTimerText: TextView
    private lateinit var nurseResponseText: TextView
    private lateinit var proceedButton: Button
    private lateinit var resendButton: Button
    private lateinit var nurseTimer: TimerUtils

    private var session: ServiceSession? = null
    private var nurseResponded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nurse_alert)

        initViews()
        setupData()
        setupClickListeners()
        startNurseAlert()
        startNurseTimer()
    }

    private fun initViews() {
        alertStatusText = findViewById(R.id.alertStatusText)
        buzzerScreenText = findViewById(R.id.buzzerScreenText)
        nurseTimerText = findViewById(R.id.nurseTimerText)
        nurseResponseText = findViewById(R.id.nurseResponseText)
        proceedButton = findViewById(R.id.proceedButton)
        resendButton = findViewById(R.id.resendButton)
    }

    private fun setupData() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val sessionJson = sharedPrefs.getString(Constants.SESSION_DATA, null)
        session = Gson().fromJson(sessionJson, ServiceSession::class.java)

        updateAlertDisplay()
    }

    private fun setupClickListeners() {
        proceedButton.setOnClickListener {
            if (nurseResponded) {
                proceedToNurseStation()
            }
        }

        resendButton.setOnClickListener {
            resendNurseAlert()
        }
    }

    private fun startNurseAlert() {
        session = session?.copy(nurseAlertTime = Date())

        // Simulate nurse response after 1 minute 25 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            simulateNurseResponse()
        }, 85000) // 1m 25s in milliseconds
    }

    private fun startNurseTimer() {
        nurseTimer = TimerUtils()
        nurseTimer.startTimer { timeString ->
            nurseTimerText.text = timeString
        }
    }

    private fun updateAlertDisplay() {
        val mealTypeText = session?.mealType?.displayName ?: getString(R.string.breakfast)
        val mealCount = session?.mealCount ?: 12
        val wardNumber = session?.wardNumber ?: "3A"
        val hostessName = session?.hostessName ?: getString(R.string.default_hostess_name)

        // Update alert status text
        alertStatusText.text = getString(
            R.string.diet_nurse_buzzer_activated,
            wardNumber,
            mealCount,
            mealTypeText
        )

        // Create initials from hostess name safely
        val hostessInitials = hostessName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString(".")

        // Update buzzer screen content
        buzzerScreenText.text = getString(
            R.string.buzzer_screen_content,
            wardNumber,
            mealTypeText.uppercase(),
            mealCount,
            hostessInitials,
            getCurrentTime(),
            if (nurseResponded) getString(R.string.mary_williams) else getString(R.string.empty_string)
        )
    }

    private fun simulateNurseResponse() {
        nurseResponded = true
        session = session?.copy(
            nurseResponseTime = Date(),
            nurseName = getString(R.string.default_nurse_name)
        )

        nurseTimer.stopTimer()

        nurseResponseText.text = getString(
            R.string.nurse_response_full,
            getString(R.string.default_nurse_name),
            getString(R.string.response_time_1m25s)
        )
        nurseResponseText.visibility = TextView.VISIBLE

        proceedButton.isEnabled = true
        proceedButton.text = getString(R.string.proceed_nurse_station)

        updateAlertDisplay()
    }

    private fun resendNurseAlert() {
        // Reset and resend alert
        nurseResponded = false
        nurseTimer.resetTimer()
        nurseResponseText.visibility = TextView.GONE
        proceedButton.isEnabled = false
        proceedButton.text = getString(R.string.waiting_nurse_response)

        startNurseAlert()
        startNurseTimer()
    }

    private fun proceedToNurseStation() {
        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            putString(Constants.SESSION_DATA, Gson().toJson(session))
        }

        // Navigate to Nurse Station
        val intent = Intent(this, NurseStationActivity::class.java)
        startActivity(intent)
    }

    private fun getCurrentTime(): String {
        val currentTime = Date()
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(currentTime)
    }

    override fun onDestroy() {
        super.onDestroy()
        nurseTimer.stopTimer()
    }
}