package co.notix.utils

import android.graphics.Color

internal object Colors {

    /**
     * Parse color from sting with exception handling
     */
    fun parseColor(color: String?): Int? {
        color ?: return null
        return try {
            Color.parseColor(color)
        } catch (e: Exception) {
            null
        }
    }
}