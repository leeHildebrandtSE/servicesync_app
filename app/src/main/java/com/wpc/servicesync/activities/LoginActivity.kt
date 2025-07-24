package com.wpc.servicesync.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit  // ✅ Import for KTX extension
import com.wpc.servicesync.R
import com.wpc.servicesync.models.Employee
import com.wpc.servicesync.utils.Constants
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {

    private lateinit var employeeIdText: TextView
    private lateinit var hostessNameText: TextView
    private lateinit var shiftScheduleText: TextView
    private lateinit var hospitalAssignmentText: TextView
    private lateinit var loginButton: Button
    private lateinit var reportsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupEmployeeData()
        setupClickListeners()
    }

    private fun initViews() {
        employeeIdText = findViewById(R.id.employeeIdText)
        hostessNameText = findViewById(R.id.hostessNameText)
        shiftScheduleText = findViewById(R.id.shiftScheduleText)
        hospitalAssignmentText = findViewById(R.id.hospitalAssignmentText)
        loginButton = findViewById(R.id.loginButton)
        reportsButton = findViewById(R.id.reportsButton)
    }

    private fun setupEmployeeData() {
        val employee = Employee(
            employeeId = "H001",
            name = "Sarah Johnson",
            shiftSchedule = "Morning Shift - 7:00 AM",
            hospitalAssignment = "General Hospital - Western Cape",
            wardAssignments = listOf("3A", "3B", "4A")
        )

        employeeIdText.text = employee.employeeId
        hostessNameText.text = employee.name
        shiftScheduleText.text = employee.shiftSchedule

        hospitalAssignmentText.text = getString(
            R.string.ward_assignments_ready,
            employee.hospitalAssignment
        )

        // ✅ Using KTX extension instead of manual edit().apply()
        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            putString(Constants.EMPLOYEE_DATA, Gson().toJson(employee))
        }
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val intent = Intent(this, KitchenExitActivity::class.java)
            startActivity(intent)
        }

        reportsButton.setOnClickListener {
            // TODO: Implement reports functionality
        }
    }
}