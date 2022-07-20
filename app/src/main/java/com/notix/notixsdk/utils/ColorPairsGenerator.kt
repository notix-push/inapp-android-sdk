package com.notix.notixsdk.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.notix.notixsdk.R
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ColorPairsGenerator {

    suspend fun generate(context: Context, drawable: Drawable?): List<Pair<Int, Int>> {
        if (drawable == null) return emptyList()
        return suspendCoroutine { continuation ->
            val bitmap = drawable.toBitmap()
            Palette.from(bitmap).generate { palette ->
                val pairs = listOfNotNull(
                    palette?.vibrantSwatch,
                    palette?.lightVibrantSwatch,
                    palette?.darkVibrantSwatch
                )
                    .map { it.bodyTextColor to it.rgb }
                    .takeIf { it.isNotEmpty() }
                    ?: listOf(getFallbackPair(context))

                continuation.resume(pairs)
            }
        }
    }

    private fun getFallbackPair(context: Context): Pair<Int, Int> =
        ContextCompat.getColor(
            context,
            R.color.notix_interstitial_text_color
        ) to ContextCompat.getColor(
            context,
            R.color.notix_interstitial_text_background_color
        )
}