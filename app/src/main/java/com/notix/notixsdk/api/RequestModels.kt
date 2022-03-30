package com.notix.notixsdk.api

import com.google.gson.annotations.SerializedName

class RequestModels {
    data class ConfigModel (
        @SerializedName("app_id")  val appId: String,
        @SerializedName("pub_id")  val pubId: Int,
        @SerializedName("sender_id")   val senderId: String,
    )

    data class ClickModel (
        @SerializedName("app_id")       var appId: String,
        @SerializedName("pub_id")       val pubId: Int,
        @SerializedName("sending_id")   val sendingId: Int,
    )

    data class ImpressionModel (
        @SerializedName("app_id")       var appId: String,
        @SerializedName("pub_id")       val pubId: Int,
        @SerializedName("sending_id")   val sendingId: Int,
    )

    data class RefreshModel (
        @SerializedName("app_id")       var appId: String,
        @SerializedName("pub_id")       val pubId: Int,
        @SerializedName("uuid")         val uuid: String,
        @SerializedName("version")      var version: String,
    )
}