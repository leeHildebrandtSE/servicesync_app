package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.wpc.servicesync.R
import com.wpc.servicesync.models.ServiceSession
import com.wpc.servicesync.utils.Constants
import com.wpc.servicesync.utils.DebugUtils
import com.wpc.servicesync.utils.QRUtils
import com.google.gson.Gson

class DebugActivity : AppCompatActivity() {

    private lateinit var sessionInfoText: TextView
    private lateinit var qrCodesText: TextView
    private lateinit var generateTestDataButton: Button
    private lateinit var clearDataButton: Button
    private lateinit var gotoKitchenExitButton: Button
    private lateinit var gotoWardArrivalButton: Button
    private lateinit var gotoDietSheetButton: Button
    private lateinit var gotoNurseAlertButton: Button
    private lateinit var gotoServingButton: Button
    private lateinit var simulateCompleteButton: Button
    private lateinit var showQRCodesButton: Button

    private var currentSession: ServiceSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Only show debug activity in debug builds
        if (!DebugUtils.isDebugMode) {
            finish()
            return
        }

        setContentView(R.layout.activity_debug)

        initViews()
        setupClickListeners()
        refreshSessionInfo()
        generateQRCodes()
    }

    private fun initViews() {
        sessionInfoText = findViewById(R.id.sessionInfoText)
        qrCodesText = findViewById(R.id.qrCodesText)
        generateTestDataButton = findViewById(R.id.generateTestDataButton)
        clearDataButton = findViewById(R.id.clearDataButton)
        gotoKitchenExitButton = findViewById(R.id.gotoKitchenExitButton)
        gotoWardArrivalButton = findViewById(R.id.gotoWardArrivalButton)
        gotoDietSheetButton = findViewById(R.id.gotoDietSheetButton)
        gotoNurseAlertButton = findViewById(R.id.gotoNurseAlertButton)
        gotoServingButton = findViewById(R.id.gotoServingButton)
        simulateCompleteButton = findViewById(R.id.simulateCompleteButton)
        showQRCodesButton = findViewById(R.id.showQRCodesButton)
    }

    private fun setupClickListeners() {
        generateTestDataButton.setOnClickListener {
            DebugUtils.saveTestDataToPreferences(this)
            refreshSessionInfo()
            Toast.makeText(this, "Test data generated", Toast.LENGTH_SHORT).show()
        }

        clearDataButton.setOnClickListener {
            DebugUtils.clearAllAppData(this)
            refreshSessionInfo()
            Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show()
        }

        gotoKitchenExitButton.setOnClickListener {
            startActivity(Intent(this, KitchenExitActivity::class.java))
        }

        gotoWardArrivalButton.setOnClickListener {
            simulateToStep("WARD_ARRIVAL")
            startActivity(Intent(this, WardArrivalActivity::class.java))
        }

        gotoDietSheetButton.setOnClickListener {
            simulateToStep("DIET_SHEET")
            startActivity(Intent(this, DietSheetActivity::class.java))
        }

        gotoNurseAlertButton.setOnClickListener {
            simulateToStep("NURSE_ALERT")
            startActivity(Intent(this, NurseAlertActivity::class.java))
        }

        gotoServingButton.setOnClickListener {
            simulateToStep("SERVICE_START")
            startActivity(Intent(this, ServingProgressActivity::class.java))
        }

        simulateCompleteButton.setOnClickListener {
            simulateToStep("COMPLETE")
            startActivity(Intent(this, ServiceCompleteActivity::class.java))
        }

        showQRCodesButton.setOnClickListener {
            generateQRCodes()
            Toast.makeText(this, "QR codes refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshSessionInfo() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val sessionJson = sharedPrefs.getString(Constants.SESSION_DATA, null)
        currentSession = if (sessionJson != null) {
            Gson().fromJson(sessionJson, ServiceSession::class.java)
        } else {
            null
        }

        val sessionInfo = if (currentSession != null) {
            DebugUtils.getSessionProgressSummary(currentSession)
        } else {
            "No session data found.\nClick 'Generate Test Data' to create sample data."
        }

        sessionInfoText.text = sessionInfo
    }

    private fun generateQRCodes() {
        val qrCodes = DebugUtils.generateAllTestQRCodes()
        val qrText = StringBuilder()
        qrText.append("Test QR Codes for Development:\n\n")

        qrCodes.forEach { (type, code) ->
            qrText.append("$type:\n$code\n\n")
        }

        qrText.append("Note: These QR codes can be used for testing the scanner functionality.")

        qrCodesText.text = qrText.toString()
    }

    private fun simulateToStep(targetStep: String) {
        if (currentSession == null) {
            DebugUtils.saveTestDataToPreferences(this)
            refreshSessionInfo()
        }

        currentSession?.let { session ->
            val simulatedSession = DebugUtils.simulateServiceProgress(session, targetStep)

            getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
                putString(Constants.SESSION_DATA, Gson().toJson(simulatedSession))
            }

            refreshSessionInfo()
            Toast.makeText(this, "Session simulated to: $targetStep", Toast.LENGTH_SHORT).show()
        }
    }
}