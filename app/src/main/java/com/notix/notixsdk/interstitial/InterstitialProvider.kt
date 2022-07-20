package com.notix.notixsdk.interstitial

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

internal class InterstitialProvider(
    private val registry: ActivityResultRegistry,
    private val callback: (InterstitialResult) -> Unit,
) : DefaultLifecycleObserver {

    private companion object {
        const val KEY = "INTERSTITIAL_CONTRACT"
    }

    private var interstitialLauncher: ActivityResultLauncher<InterstitialData>? = null

    override fun onCreate(owner: LifecycleOwner) {
        interstitialLauncher = registry
            .register(KEY, owner, InterstitialContract(), ::handleResult)
    }

    fun showInterstitial(data: InterstitialData) {
        interstitialLauncher?.launch(data)
    }

    private fun handleResult(result: InterstitialResult) {
        callback(result)
    }
}