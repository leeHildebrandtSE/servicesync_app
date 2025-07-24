// File: app/src/main/java/com/wpc/servicesync/utils/QRUtils.kt
package com.wpc.servicesync.utils

import androidx.activity.result.ActivityResultLauncher
import com.journeyapps.barcodescanner.ScanOptions

object QRUtils {

    enum class QRType(val prefix: String) {
        KITCHEN_EXIT("KITCHEN_"),
        WARD_ARRIVAL("WARD_"),
        NURSE_STATION("NURSE_")
    }

    fun launchQRScanner(launcher: ActivityResultLauncher<ScanOptions>, prompt: String) {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt(prompt)
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        launcher.launch(options)
    }

    fun validateQRCode(qrCode: String, expectedType: QRType): Boolean {
        return qrCode.startsWith(expectedType.prefix, ignoreCase = true)
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
            val keyValue = part.split("=")
            if (keyValue.size == 2) {
                data[keyValue[0]] = keyValue[1]
            }
        }

        return data
    }

    fun generateQRCode(type: QRType, data: Map<String, String>): String {
        val content = data.map { "${it.key}=${it.value}" }.joinToString("|")
        return "${type.prefix}$content"
    }
}