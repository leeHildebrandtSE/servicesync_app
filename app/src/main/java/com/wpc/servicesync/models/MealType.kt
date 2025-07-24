package com.wpc.servicesync.models

/**
 * Enum representing different meal types for hospital meal delivery
 */
enum class MealType(
    val displayName: String,
    val icon: String,
    val timeRange: String
) {
    BREAKFAST(
        displayName = "Breakfast",
        icon = "🌅",
        timeRange = "06:00 - 09:00"
    ),

    LUNCH(
        displayName = "Lunch",
        icon = "☀️",
        timeRange = "11:30 - 14:00"
    ),

    SUPPER(
        displayName = "Supper",
        icon = "🌙",
        timeRange = "17:00 - 19:30"
    ),

    BEVERAGES(
        displayName = "Beverages",
        icon = "☕",
        timeRange = "All Day"
    );

    /**
     * Get display name with icon
     */
    fun getDisplayNameWithIcon(): String {
        return "$icon $displayName"
    }

    /**
     * Get lowercase display name for use in sentences
     */
    fun getLowercaseDisplayName(): String {
        return displayName.lowercase()
    }

    companion object {
        /**
         * Get MealType from string name (case insensitive)
         */
        fun fromString(name: String): MealType? {
            return entries.find {
                it.name.equals(name, ignoreCase = true) ||
                        it.displayName.equals(name, ignoreCase = true)
            }
        }

        /**
         * Get all meal types as a list for UI display
         */
        fun getAllForDisplay(): List<MealType> {
            return entries.toList()
        }

        /**
         * Get meal types for current time (basic logic)
         */
        fun getCurrentMealTypes(): List<MealType> {
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

            return when (currentHour) {
                in 6..10 -> listOf(BREAKFAST, BEVERAGES)
                in 11..15 -> listOf(LUNCH, BEVERAGES)
                in 16..20 -> listOf(SUPPER, BEVERAGES)
                else -> listOf(BEVERAGES)
            }
        }
    }
}