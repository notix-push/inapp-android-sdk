package com.notix.notixsdk.data

class RequestModels {
    data class ConfigModel (
        val appId: String,
        val pubId: Int,
        val senderId: Long,
    )

    data class ClickModel (
        var appId: String,
        val pubId: Int,
        val sendingId: Int,
    )

    data class ImpressionModel (
        var appId: String,
        val pubId: Int,
        val sendingId: Int,
    )

    data class RefreshModel (
        var appId: String,
        val pubId: Int,
        val uuid: String,
        var version: String,
    )
}