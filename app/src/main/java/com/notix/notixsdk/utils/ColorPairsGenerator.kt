package com.notix.notixsdk.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.palette.graphics.Palette
import com.notix.notixsdk.R

object ColorPairsGenerator {

    fun generate(context: Context, drawable: Drawable?, onResult: (List<Pair<Int, Int>>) -> Unit) {
        if (drawable == null) onResult(emptyList())
        Thread {
            val bitmap = drawable!!.toBitmap()
            Palette.from(bitmap).generate { palette ->
                val pairs = listOfNotNull(
                    palette?.vibrantSwatch,
                    palette?.lightVibrantSwatch,
                    palette?.darkVibrantSwatch
                )
                    .map { it.bodyTextColor to it.rgb }
                    .takeIf { it.isNotEmpty() }
                    ?: listOf(getFallbackPair(context))

                onResult(pairs)
            }
        }.start()
    }

    private fun getFallbackPair(context: Context): Pair<Int, Int> = if (Build.VERSION.SDK_INT >= 23) {
        context.getColor(
            R.color.notix_interstitial_text_color
        ) to context.getColor(
            R.color.notix_interstitial_text_background_color
        )
    } else {
        context.resources.getColor(
            R.color.notix_interstitial_text_color
        ) to context.resources.getColor(
            R.color.notix_interstitial_text_background_color
        )
    }
}