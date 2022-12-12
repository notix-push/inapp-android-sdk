package com.notix.notixsdk.interstitial

import android.content.Context
import android.content.Intent
import android.util.Log

class InterstitialContract {

    companion object {
        const val RESULT_CLICKED = 1234
        const val RESULT_DISMISSED = 5678
        const val RESULT_ERROR = 6543
        const val REQUEST_CODE = 6548

        fun createIntent(context: Context, input: InterstitialData?) =
            Intent(context, InterstitialActivity::class.java)
                .also {
                    Log.i("NotixDebug", "data before put: $input")
                }
                .apply { putExtra(InterstitialActivity.DATA, input) }

        fun parseResult(resultCode: Int, intent: Intent?) = when (resultCode) {
            RESULT_CLICKED -> InterstitialResult.Clicked
            RESULT_DISMISSED -> InterstitialResult.Dismissed
            else -> InterstitialResult.Error
        }
    }
}

enum class InterstitialResult {
    Clicked, Dismissed, Error
}
