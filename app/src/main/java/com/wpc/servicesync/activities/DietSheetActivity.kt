package com.wpc.servicesync.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.wpc.servicesync.R
import com.wpc.servicesync.models.ServiceSession
import com.wpc.servicesync.utils.Constants
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DietSheetActivity : AppCompatActivity() {

    private lateinit var capturePhotoButton: Button
    private lateinit var retakePhotoButton: Button
    private lateinit var skipDocumentationButton: Button
    private lateinit var proceedButton: Button
    private lateinit var photoPreview: ImageView
    private lateinit var photoStatusText: TextView
    private lateinit var photoTimeText: TextView
    private lateinit var additionalNotesEditText: EditText

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var currentPhotoPath: String? = null
    private var session: ServiceSession? = null
    private var photoTaken = false

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_sheet)

        initViews()
        setupData()
        setupCameraLauncher()
        setupClickListeners()
        checkCameraPermission()
    }

    private fun initViews() {
        capturePhotoButton = findViewById(R.id.capturePhotoButton)
        retakePhotoButton = findViewById(R.id.retakePhotoButton)
        skipDocumentationButton = findViewById(R.id.skipDocumentationButton)
        proceedButton = findViewById(R.id.proceedButton)
        photoPreview = findViewById(R.id.photoPreview)
        photoStatusText = findViewById(R.id.photoStatusText)
        photoTimeText = findViewById(R.id.photoTimeText)
        additionalNotesEditText = findViewById(R.id.additionalNotesEditText)
    }

    private fun setupData() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        val sessionJson = sharedPrefs.getString(Constants.SESSION_DATA, null)
        session = Gson().fromJson(sessionJson, ServiceSession::class.java)

        // Initially disable proceed button
        proceedButton.isEnabled = false
        retakePhotoButton.visibility = Button.GONE
        photoPreview.visibility = ImageView.GONE
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                handlePhotoCapture()
            }
        }
    }

    private fun setupClickListeners() {
        capturePhotoButton.setOnClickListener {
            if (checkCameraPermission()) {
                takePhoto()
            }
        }

        retakePhotoButton.setOnClickListener {
            takePhoto()
        }

        skipDocumentationButton.setOnClickListener {
            proceedToNurseAlert()
        }

        proceedButton.setOnClickListener {
            if (photoTaken) {
                saveDietSheetData()
                proceedToNurseAlert()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto()
                } else {
                    Toast.makeText(this, "Camera permission is required to capture diet sheets", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun takePhoto() {
        val photoFile = try {
            createImageFile()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
            return
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }

            cameraLauncher.launch(takePictureIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "DIET_SHEET_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun handlePhotoCapture() {
        currentPhotoPath?.let { path ->
            val bitmap = BitmapFactory.decodeFile(path)
            photoPreview.setImageBitmap(bitmap)
            photoPreview.visibility = ImageView.VISIBLE

            // Update UI
            photoTaken = true
            photoStatusText.text = "Diet sheet captured ✓"
            photoStatusText.setTextColor(ContextCompat.getColor(this, R.color.success_dark))

            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            photoTimeText.text = currentTime
            photoTimeText.setTextColor(ContextCompat.getColor(this, R.color.success_dark))

            // Show validation card
            val validationCard = findViewById<androidx.cardview.widget.CardView>(R.id.validationCard)
            validationCard.visibility = androidx.cardview.widget.CardView.VISIBLE

            // Enable proceed button and show retake option
            proceedButton.isEnabled = true
            proceedButton.text = "📋 Confirm & Alert Nurse"
            retakePhotoButton.visibility = Button.VISIBLE
            capturePhotoButton.text = "📷 Photo Captured"

            Toast.makeText(this, "Diet sheet photo captured successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDietSheetData() {
        val additionalNotes = additionalNotesEditText.text.toString()

        session = session?.copy(
            dietSheetPhotoPath = currentPhotoPath,
            dietSheetCaptureTime = Date(),
            dietSheetNotes = additionalNotes,
            dietSheetDocumented = photoTaken,
            comments = session?.comments?.let { existing ->
                if (additionalNotes.isNotEmpty()) {
                    "$existing\nDiet Sheet Notes: $additionalNotes"
                } else existing
            } ?: additionalNotes
        )

        getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE).edit {
            putString(Constants.SESSION_DATA, Gson().toJson(session))
        }
    }

    private fun proceedToNurseAlert() {
        val intent = Intent(this, NurseAlertActivity::class.java)
        startActivity(intent)
    }
}