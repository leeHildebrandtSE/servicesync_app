package com.wpc.servicesync.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.wpc.servicesync.R
import com.wpc.servicesync.utils.QRUtils
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class QRScannerFragment : Fragment() {

    private var rootView: View? = null

    private var scanButton: Button? = null
    private var scanStatusText: TextView? = null
    private lateinit var qrScannerLauncher: ActivityResultLauncher<ScanOptions>

    private var qrType: QRUtils.QRType = QRUtils.QRType.KITCHEN_EXIT
    private var onScanResult: ((String) -> Unit)? = null
    private var scanPrompt: String = "Scan QR Code"

    companion object {
        private const val ARG_QR_TYPE = "qr_type"
        private const val ARG_SCAN_PROMPT = "scan_prompt"

        fun newInstance(
            qrType: QRUtils.QRType,
            scanPrompt: String = "Scan QR Code"
        ): QRScannerFragment {
            val fragment = QRScannerFragment()
            val args = Bundle().apply {
                putString(ARG_QR_TYPE, qrType.name)
                putString(ARG_SCAN_PROMPT, scanPrompt)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { args ->
            qrType = QRUtils.QRType.valueOf(
                args.getString(ARG_QR_TYPE, QRUtils.QRType.KITCHEN_EXIT.name)
            )
            scanPrompt = args.getString(ARG_SCAN_PROMPT, "Scan QR Code")
        }

        setupQRScanner()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_qr_scanner, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        updateUI()
    }

    private fun initViews(view: View) {
        scanButton = view.findViewById<Button>(R.id.scanButton)
        scanStatusText = view.findViewById<TextView>(R.id.scanStatusText)

        // Debug: Check if views were found
        if (scanButton == null) {
            throw IllegalStateException("scanButton not found in layout. Check that R.id.scanButton exists in fragment_qr_scanner.xml")
        }
        if (scanStatusText == null) {
            throw IllegalStateException("scanStatusText not found in layout. Check that R.id.scanStatusText exists in fragment_qr_scanner.xml")
        }
    }

    private fun setupQRScanner() {
        qrScannerLauncher = registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val qrCode = result.contents
                handleScanResult(qrCode)
            }
        }
    }

    private fun setupClickListeners() {
        scanButton?.setOnClickListener {
            launchQRScanner()
        }
    }

    private fun updateUI() {
        val (buttonText, statusText) = when (qrType) {
            QRUtils.QRType.KITCHEN_EXIT -> {
                getString(R.string.scan_kitchen_exit_qr) to getString(R.string.position_qr_frame)
            }
            QRUtils.QRType.WARD_ARRIVAL -> {
                "Scan Ward QR" to getString(R.string.position_qr_frame)
            }
            QRUtils.QRType.NURSE_STATION -> {
                getString(R.string.scan_nurse_station_qr) to getString(R.string.final_qr_start_serving)
            }
        }

        scanButton?.text = buttonText
        scanStatusText?.text = statusText
    }

    private fun launchQRScanner() {
        QRUtils.launchQRScanner(qrScannerLauncher, scanPrompt)
    }

    private fun handleScanResult(qrCode: String) {
        if (QRUtils.validateQRCode(qrCode, qrType)) {
            showScanSuccess()
            onScanResult?.invoke(qrCode)
        } else {
            showScanError()
        }
    }

    private fun showScanSuccess() {
        val successMessage = when (qrType) {
            QRUtils.QRType.KITCHEN_EXIT -> getString(R.string.kitchen_exit_success)
            QRUtils.QRType.WARD_ARRIVAL -> getString(R.string.ward_qr_scanned_success)
            QRUtils.QRType.NURSE_STATION -> getString(R.string.nurse_station_qr_success)
        }

        scanStatusText?.text = getString(R.string.success_qr_scanned)
        scanButton?.text = "✅ QR Scanned"
        scanButton?.isEnabled = false

        Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showScanError() {
        val errorMessage = when (qrType) {
            QRUtils.QRType.KITCHEN_EXIT -> getString(R.string.invalid_kitchen_qr)
            QRUtils.QRType.WARD_ARRIVAL -> getString(R.string.invalid_ward_qr)
            QRUtils.QRType.NURSE_STATION -> getString(R.string.invalid_nurse_station_qr)
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun setOnScanResultListener(listener: (String) -> Unit) {
        onScanResult = listener
    }

    fun resetScanner() {
        scanButton?.isEnabled = true
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanButton = null
        scanStatusText = null
        rootView = null
    }
}