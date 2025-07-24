// File: app/src/main/java/com/wpc/servicesync/utils/StringUtils.kt
package com.wpc.servicesync.utils

import java.util.Locale

object StringUtils {

    fun formatString(format: String, vararg args: Any): String {
        return String.format(Locale.getDefault(), format, *args)
    }

    fun formatProgress(current: Int, total: Int): String {
        return "$current / $total"
    }

    fun capitalizeFirst(text: String): String {
        return if (text.isEmpty()) text else text.first().uppercaseChar() + text.drop(1)
    }

    fun initials(fullName: String): String {
        return fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString(".")
    }

    fun formatPercentage(value: Float): String {
        return String.format(Locale.getDefault(), "%.1f%%", value)
    }

    fun sanitizeInput(input: String): String {
        return input.trim().replace(Regex("[^a-zA-Z0-9\\s.-]"), "")
    }

    fun truncate(text: String, maxLength: Int, ellipsis: String = "..."): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - ellipsis.length) + ellipsis
        }
    }
}