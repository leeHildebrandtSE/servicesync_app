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
import com.wpc.servicesync.models.Employee
import com.wpc.servicesync.models.ServiceSession
import com.wpc.servicesync.models.MealType
import com.wpc.servicesync.utils.Constants
import com.wpc.servicesync.utils.QRUtils
import com.wpc.servicesync.utils.TimerUtils
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.Date
import java.util.UUID

class KitchenExitActivity : AppCompatActivity() {

    private lateinit var scanAreaButton: Button
    private lateinit var confirmExitButton: Button
    private lateinit var manualEntryButton: Button
    private lateinit var statusText: TextView
    private lateinit var qrScannerLauncher: ActivityResultLauncher<ScanOptions>
    private lateinit var timer: TimerUtils

    private var session: ServiceSession? = null
    private var employee: Employee? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kitchen_exit)

        initViews()
        setupData()
        setupQRScanner()
        setupClickListeners()
        startTimer()
    }

    private fun initViews() {
        scanAreaButton = findViewById(R.id.scanAreaButton)
        confirmExitButton = findViewById(R.id.confirmExitButton)
        manualEntryButton = findViewById(R.id.manualEntryButton)
        statusText = findViewById(R.id.statusText)
    }

    private fun setupData() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val employeeJson = sharedPrefs.getString(Constants.EMPLOYEE_DATA, null)
        employee = Gson().fromJson(employeeJson, Employee::class.java)

        // Create new session
        session = ServiceSession(
            sessionId = UUID.randomUUID().toString(),
            employeeId = employee?.employeeId ?: "",
            hostessName = employee?.name ?: "",
            shiftSchedule = employee?.shiftSchedule ?: "",
            wardNumber = "3A", // Default ward number
            wardName = "General Medicine", // Default ward name
            mealType = MealType.BREAKFAST,
            mealCount = 12
        )
    }

    private fun setupQRScanner() {
        qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val qrCode = result.contents
                if (QRUtils.validateQRCode(qrCode, QRUtils.QRType.KITCHEN_EXIT)) {
                    handleKitchenExitScan(qrCode)
                } else {
                    Toast.makeText(this, getString(R.string.invalid_kitchen_qr), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        scanAreaButton.setOnClickListener {
            QRUtils.launchQRScanner(qrScannerLauncher, "Scan Kitchen Exit QR Code")
        }

        confirmExitButton.setOnClickListener {
            confirmKitchenExit()
        }

        manualEntryButton.setOnClickListener {
            // TODO: Implement manual entry
            confirmKitchenExit()
        }
    }

    private fun startTimer() {
        timer = TimerUtils()
        timer.startTimer { timeString ->
            // Update any timer displays if needed
        }
    }

    private fun handleKitchenExitScan(qrCode: String) {
        session = session?.copy(kitchenExitTime = Date())

        statusText.text = getString(R.string.kitchen_exit_qr_scanned_success)
        confirmExitButton.isEnabled = true

        Toast.makeText(this, getString(R.string.kitchen_exit_success), Toast.LENGTH_SHORT).show()
    }

    private fun confirmKitchenExit() {
        session = session?.copy(kitchenExitTime = Date())

        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            putString(Constants.SESSION_DATA, Gson().toJson(session))
        }

        // Navigate to Ward Arrival
        val intent = Intent(this, WardArrivalActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.stopTimer()
    }
}