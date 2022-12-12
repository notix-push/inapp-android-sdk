package com.notix.notixsdk.interstitial

import android.app.Activity
import android.content.Context
import android.content.Intent

internal class InterstitialProvider(
    private val activity: Activity?
) {

    private companion object {
        const val KEY = "INTERSTITIAL_CONTRACT"
    }

    fun showInterstitial(context: Context, data: InterstitialData) {
        if (activity == null) {
            val intent = InterstitialContract.createIntent(context, data)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            activity.startActivityForResult(
                InterstitialContract.createIntent(activity, data),
                InterstitialContract.REQUEST_CODE
            )
        }
    }
}