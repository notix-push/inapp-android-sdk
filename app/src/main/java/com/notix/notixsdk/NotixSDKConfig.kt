package com.notix.notixsdk

import com.notix.notixsdk.domain.DomainModels

data class NotixSDKConfig(
    var interstitialStartupEnabled: Boolean = true,
    var interstitialDefaultZoneId: Long = 0L,
    var interstitialDefaultVars: DomainModels.RequestVars = DomainModels.RequestVars(),
    var customUserAgent: String? = null,
)