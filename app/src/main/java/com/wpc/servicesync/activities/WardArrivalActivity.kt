package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.edit
import com.wpc.servicesync.R
import com.wpc.servicesync.adapters.MealSelectionAdapter
import com.wpc.servicesync.models.MealType
import com.wpc.servicesync.models.ServiceSession
import com.wpc.servicesync.utils.Constants
import com.wpc.servicesync.utils.QRUtils
import com.wpc.servicesync.utils.TimerUtils
import com.google.gson.Gson
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.Date
import java.util.Locale

class WardArrivalActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var mealSelectionRecyclerView: RecyclerView
    private lateinit var mealCountInput: EditText
    private lateinit var minusButton: Button
    private lateinit var plusButton: Button
    private lateinit var commentsEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var mealSelectionAdapter: MealSelectionAdapter
    private lateinit var qrScannerLauncher: ActivityResultLauncher<ScanOptions>
    private lateinit var timer: TimerUtils

    private var session: ServiceSession? = null
    private var selectedMealType: MealType = MealType.BREAKFAST
    private var mealCount: Int = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ward_arrival)

        initViews()
        setupData()
        setupMealSelection()
        setupQRScanner()
        setupClickListeners()
        startTimer()

        // Simulate QR scan success
        simulateWardArrival()
    }

    private fun initViews() {
        timerText = findViewById(R.id.timerText)
        mealSelectionRecyclerView = findViewById(R.id.mealSelectionRecyclerView)
        mealCountInput = findViewById(R.id.mealCountInput)
        minusButton = findViewById(R.id.minusButton)
        plusButton = findViewById(R.id.plusButton)
        commentsEditText = findViewById(R.id.commentsEditText)
        confirmButton = findViewById(R.id.confirmButton)
    }

    private fun setupData() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val sessionJson = sharedPrefs.getString(Constants.SESSION_DATA, null)
        session = Gson().fromJson(sessionJson, ServiceSession::class.java)

        // Locale-safe formatting
        mealCountInput.setText(String.format(Locale.getDefault(), "%d", session?.mealCount ?: 12))
        mealCount = session?.mealCount ?: 12
    }

    private fun setupMealSelection() {
        mealSelectionAdapter = MealSelectionAdapter(
            MealType.getAllForDisplay(),
            selectedMealType
        ) { mealType ->
            selectedMealType = mealType
        }

        mealSelectionRecyclerView.layoutManager = GridLayoutManager(this, 2)
        mealSelectionRecyclerView.adapter = mealSelectionAdapter
    }

    private fun setupQRScanner() {
        qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val qrCode = result.contents
                if (QRUtils.validateQRCode(qrCode, QRUtils.QRType.WARD_ARRIVAL)) {
                    handleWardArrivalScan(qrCode)
                } else {
                    Toast.makeText(this, getString(R.string.invalid_ward_qr), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        minusButton.setOnClickListener {
            if (mealCount > Constants.MEAL_COUNT_MIN) {
                mealCount--
                mealCountInput.setText(String.format(Locale.getDefault(), "%d", mealCount))
            }
        }

        plusButton.setOnClickListener {
            if (mealCount < Constants.MEAL_COUNT_MAX) {
                mealCount++
                mealCountInput.setText(String.format(Locale.getDefault(), "%d", mealCount))
            }
        }

        mealCountInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val input = mealCountInput.text.toString().toIntOrNull()
                if (input != null && input >= Constants.MEAL_COUNT_MIN && input <= Constants.MEAL_COUNT_MAX) {
                    mealCount = input
                } else {
                    mealCountInput.setText(String.format(Locale.getDefault(), "%d", mealCount))
                }
            }
        }

        confirmButton.setOnClickListener {
            confirmWardArrival()
        }
    }

    private fun startTimer() {
        timer = TimerUtils()
        timer.startTimer { timeString ->
            timerText.text = timeString
        }
    }

    private fun simulateWardArrival() {
        // Simulate successful ward QR scan
        session = session?.copy(wardArrivalTime = Date())
        Toast.makeText(this, "Ward 3A QR Code Scanned Successfully", Toast.LENGTH_SHORT).show()
    }

    private fun handleWardArrivalScan(qrCode: String) {
        session = session?.copy(wardArrivalTime = Date())
        Toast.makeText(this, getString(R.string.ward_arrival_success), Toast.LENGTH_SHORT).show()
    }

    private fun confirmWardArrival() {
        val comments = commentsEditText.text.toString()

        session = session?.copy(
            wardArrivalTime = session?.wardArrivalTime ?: Date(),
            mealType = selectedMealType,
            mealCount = mealCount,
            comments = comments
        )

        // Save session data
        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            putString(Constants.SESSION_DATA, Gson().toJson(session))
        }

        // Navigate to Diet Sheet Activity instead of directly to Nurse Alert
        val intent = Intent(this, DietSheetActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.stopTimer()
    }
}