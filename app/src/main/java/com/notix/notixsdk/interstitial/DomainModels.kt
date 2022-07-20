package com.notix.notixsdk.interstitial

import java.io.Serializable

internal data class InterstitialData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val iconUrl: String,
    val targetUrl: String,
    val openExternalBrowser: Boolean,
    val impressionData: String,
    val buttons: List<InterstitialButton>,
    val closingSettings: ClosingSettings,
) : Serializable

data class InterstitialButton(
    val text: String,
    val targetUrl: String? = null,  // InterstitialData.targetUrl is used if null
) : Serializable

data class ClosingSettings(
    val timeout: Int, // seconds
    val opacity: Float = 1f, // 0 to 1
    val sizePercent: Float = 1f, // 0 to 2
) : Serializable