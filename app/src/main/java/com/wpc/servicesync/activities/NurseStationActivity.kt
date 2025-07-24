package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.wpc.servicesync.R
import com.wpc.servicesync.models.ServiceSession
import com.wpc.servicesync.utils.Constants
import com.wpc.servicesync.utils.QRUtils
import com.wpc.servicesync.utils.TimerUtils
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.Date

class NurseStationActivity : AppCompatActivity() {

    private lateinit var scanAreaButton: Button
    private lateinit var startServingButton: Button
    private lateinit var viewPatientListButton: Button
    private lateinit var nurseInfoText: TextView
    private lateinit var totalTimerText: TextView
    private lateinit var serviceSummaryText: TextView
    private lateinit var qrScannerLauncher: ActivityResultLauncher<ScanOptions>
    private lateinit var totalTimer: TimerUtils

    private var session: ServiceSession? = null
    private var nurseStationScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nurse_station)

        initViews()
        setupData()
        setupQRScanner()
        setupClickListeners()
        startTotalTimer()
        updateServiceSummary()
    }

    private fun initViews() {
        scanAreaButton = findViewById(R.id.scanAreaButton)
        startServingButton = findViewById(R.id.startServingButton)
        viewPatientListButton = findViewById(R.id.viewPatientListButton)
        nurseInfoText = findViewById(R.id.nurseInfoText)
        totalTimerText = findViewById(R.id.totalTimerText)
        serviceSummaryText = findViewById(R.id.serviceSummaryText)
    }

    private fun setupData() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val sessionJson = sharedPrefs.getString(Constants.SESSION_DATA, null)
        session = Gson().fromJson(sessionJson, ServiceSession::class.java)

        val nurseInfo = getString(
            R.string.nurse_station_info,
            session?.wardNumber ?: "3A",
            session?.nurseName ?: "Mary Williams",
            session?.mealCount ?: 12,
            session?.mealType?.displayName?.lowercase() ?: getString(R.string.breakfast)
        )

        nurseInfoText.text = nurseInfo
    }

    private fun setupQRScanner() {
        qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val qrCode = result.contents
                if (QRUtils.validateQRCode(qrCode, QRUtils.QRType.NURSE_STATION)) {
                    handleNurseStationScan(qrCode)
                } else {
                    Toast.makeText(this, getString(R.string.invalid_nurse_station_qr), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        scanAreaButton.setOnClickListener {
            QRUtils.launchQRScanner(qrScannerLauncher, "Scan Nurse Station QR Code")
        }

        startServingButton.setOnClickListener {
            if (nurseStationScanned) {
                startServing()
            } else {
                Toast.makeText(this, getString(R.string.scan_nurse_station_first), Toast.LENGTH_SHORT).show()
            }
        }

        viewPatientListButton.setOnClickListener {
            Toast.makeText(this, getString(R.string.patient_list_coming_soon), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTotalTimer() {
        totalTimer = TimerUtils()

        val kitchenExitTime = session?.kitchenExitTime?.time ?: System.currentTimeMillis()
        val elapsedMillis = System.currentTimeMillis() - kitchenExitTime

        totalTimer.startTimer { timeString ->
            totalTimerText.text = timeString
        }
    }

    private fun handleNurseStationScan(qrCode: String) {
        nurseStationScanned = true
        Toast.makeText(this, getString(R.string.nurse_station_qr_success), Toast.LENGTH_SHORT).show()

        startServingButton.isEnabled = true
        startServingButton.text = getString(R.string.start_serving)

        scanAreaButton.text = getString(R.string.nurse_station_qr_scanned)
        scanAreaButton.isEnabled = false
    }

    private fun updateServiceSummary() {
        val currentSession = session
        if (currentSession == null) {
            serviceSummaryText.text = getString(R.string.no_session_data)
            return
        }

        val kitchenExitTime = currentSession.kitchenExitTime
        val wardArrivalTime = currentSession.wardArrivalTime
        val nurseResponseTime = currentSession.nurseResponseTime
        val nurseAlertTime = currentSession.nurseAlertTime

        val travelTime = if (kitchenExitTime != null && wardArrivalTime != null) {
            TimerUtils.formatTimeFromSeconds((wardArrivalTime.time - kitchenExitTime.time) / 1000)
        } else getString(R.string.na)

        val nurseResponseDuration = if (nurseAlertTime != null && nurseResponseTime != null) {
            TimerUtils.formatTimeFromSeconds((nurseResponseTime.time - nurseAlertTime.time) / 1000)
        } else getString(R.string.na)

        val summary = getString(
            R.string.service_summary_content,
            formatTime(kitchenExitTime),
            formatTime(wardArrivalTime),
            travelTime,
            formatTime(nurseAlertTime),
            nurseResponseDuration,
            currentSession.mealCount
        )

        serviceSummaryText.text = summary
    }

    private fun formatTime(date: Date?): String {
        if (date == null) return getString(R.string.na)
        val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return format.format(date)
    }

    private fun startServing() {
        session = session?.copy(serviceStartTime = Date())

        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            putString(Constants.SESSION_DATA, Gson().toJson(session))
        }

        val intent = Intent(this, ServingProgressActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        totalTimer.stopTimer()
    }
}