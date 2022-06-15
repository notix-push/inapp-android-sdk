package com.notix.notixsdk.interstitial

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

internal class InterstitialContract :
    ActivityResultContract<InterstitialData, InterstitialResult>() {

    companion object {
        const val RESULT_CLICKED = 1234
        const val RESULT_DISMISSED = 5678
        const val RESULT_ERROR = 6543
    }

    override fun createIntent(context: Context, input: InterstitialData?) =
        Intent(context, InterstitialActivity::class.java)
            .apply { putExtra(InterstitialActivity.DATA, input) }

    override fun parseResult(resultCode: Int, intent: Intent?) = when (resultCode) {
        RESULT_CLICKED -> InterstitialResult.Clicked
        RESULT_DISMISSED -> InterstitialResult.Dismissed
        else -> InterstitialResult.Error
    }
}

enum class InterstitialResult {
    Clicked, Dismissed, Error
}
