package co.notix

import co.notix.domain.RequestVars

public data class NotixSDKConfig(
    var interstitialStartupEnabled: Boolean = true,
    var interstitialDefaultZoneId: Long = 0L,
    var interstitialDefaultVars: RequestVars = RequestVars(),
    var customUserAgent: String? = null,
)