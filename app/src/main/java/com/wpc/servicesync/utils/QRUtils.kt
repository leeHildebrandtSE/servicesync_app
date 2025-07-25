package com.wpc.servicesync.utils

import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanOptions
import java.util.Date

object QRUtils {

    enum class QRType(val prefix: String, val displayName: String) {
        KITCHEN_EXIT("KITCHEN_", "Kitchen Exit"),
        WARD_ARRIVAL("WARD_", "Ward Arrival"),
        NURSE_STATION("NURSE_", "Nurse Station")
    }

    fun launchQRScanner(launcher: ActivityResultLauncher<ScanOptions>, prompt: String) {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt(prompt)
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
            setTimeout(30000) // 30 second timeout
        }
        launcher.launch(options)
    }

    fun validateQRCode(qrCode: String, expectedType: QRType): Boolean {
        return qrCode.startsWith(expectedType.prefix, ignoreCase = true) &&
                qrCode.length > expectedType.prefix.length
    }

    fun extractDataFromQR(qrCode: String, type: QRType): Map<String, String> {
        val data = mutableMapOf<String, String>()

        if (!validateQRCode(qrCode, type)) {
            return data
        }

        // Remove prefix and parse data
        val content = qrCode.removePrefix(type.prefix)
        val parts = content.split("|")

        for (part in parts) {
            val keyValue = part.split("=", limit = 2)
            if (keyValue.size == 2) {
                data[keyValue[0].trim()] = keyValue[1].trim()
            }
        }

        return data
    }

    fun generateQRCode(type: QRType, data: Map<String, String>): String {
        val content = data.map { "${it.key}=${it.value}" }.joinToString("|")
        return "${type.prefix}$content"
    }

    // Test QR Code Generation for Development
    fun generateTestQRCodes(): Map<QRType, String> {
        val testQRCodes = mutableMapOf<QRType, String>()

        // Kitchen Exit QR
        testQRCodes[QRType.KITCHEN_EXIT] = generateQRCode(
            QRType.KITCHEN_EXIT,
            mapOf(
                "location" to "MAIN_KITCHEN",
                "station" to "EXIT_001",
                "timestamp" to Date().time.toString()
            )
        )

        // Ward Arrival QR
        testQRCodes[QRType.WARD_ARRIVAL] = generateQRCode(
            QRType.WARD_ARRIVAL,
            mapOf(
                "ward" to "3A",
                "department" to "GENERAL_MEDICINE",
                "floor" to "3",
                "timestamp" to Date().time.toString()
            )
        )

        // Nurse Station QR
        testQRCodes[QRType.NURSE_STATION] = generateQRCode(
            QRType.NURSE_STATION,
            mapOf(
                "ward" to "3A",
                "station" to "NURSE_STATION_001",
                "verified" to "true",
                "timestamp" to Date().time.toString()
            )
        )

        return testQRCodes
    }

    fun isValidQRFormat(qrCode: String): Boolean {
        // Basic validation - QR code should not be empty and should have reasonable length
        return qrCode.isNotBlank() && qrCode.length >= 8 && qrCode.length <= 500
    }

    fun getQRTypeFromCode(qrCode: String): QRType? {
        return QRType.values().find { type ->
            qrCode.startsWith(type.prefix, ignoreCase = true)
        }
    }

    fun formatQRValidationError(qrCode: String, expectedType: QRType): String {
        return when {
            qrCode.isBlank() -> "QR code is empty"
            !isValidQRFormat(qrCode) -> "Invalid QR code format"
            !qrCode.startsWith(expectedType.prefix, ignoreCase = true) ->
                "Expected ${expectedType.displayName} QR code, but found different type"
            else -> "QR code validation failed"
        }
    }

    // Utility function to simulate QR scanning for testing
    fun simulateQRScan(type: QRType): String {
        return when (type) {
            QRType.KITCHEN_EXIT -> generateQRCode(
                type,
                mapOf(
                    "location" to "MAIN_KITCHEN",
                    "exit_point" to "DELIVERY_DOOR_1",
                    "scan_time" to Date().time.toString()
                )
            )
            QRType.WARD_ARRIVAL -> generateQRCode(
                type,
                mapOf(
                    "ward_number" to "3A",
                    "ward_name" to "GENERAL_MEDICINE",
                    "arrival_point" to "WARD_ENTRANCE",
                    "scan_time" to Date().time.toString()
                )
            )
            QRType.NURSE_STATION -> generateQRCode(
                type,
                mapOf(
                    "station_id" to "NS_3A_001",
                    "ward" to "3A",
                    "authorized" to "true",
                    "scan_time" to Date().time.toString()
                )
            )
        }
    }

    // Function to get QR code info for display
    fun getQRCodeInfo(qrCode: String): QRCodeInfo {
        val type = getQRTypeFromCode(qrCode)
        val data = if (type != null) extractDataFromQR(qrCode, type) else emptyMap()

        return QRCodeInfo(
            type = type,
            isValid = type != null && validateQRCode(qrCode, type),
            data = data,
            rawCode = qrCode
        )
    }

    data class QRCodeInfo(
        val type: QRType?,
        val isValid: Boolean,
        val data: Map<String, String>,
        val rawCode: String
    ) {
        fun getDisplayInfo(): String {
            return if (isValid && type != null) {
                val mainInfo = when (type) {
                    QRType.KITCHEN_EXIT -> "Kitchen: ${data["location"] ?: "Unknown"}"
                    QRType.WARD_ARRIVAL -> "Ward: ${data["ward"] ?: data["ward_number"] ?: "Unknown"}"
                    QRType.NURSE_STATION -> "Station: ${data["station_id"] ?: "Unknown"}"
                }
                "$mainInfo (${type.displayName})"
            } else {
                "Invalid QR Code"
            }
        }
    }
}