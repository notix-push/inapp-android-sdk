package com.notix.notixsdk.interstitial

import java.io.Serializable

data class InterstitialData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val iconUrl: String,
    val targetUrl: String,
    val openExternalBrowser: Boolean,
    val impressionData: String,
) : Serializable