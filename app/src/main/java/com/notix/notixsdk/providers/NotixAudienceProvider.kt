package com.notix.notixsdk.providers

import android.content.Context
import com.notix.notixsdk.api.ApiClient

class NotixAudienceProvider {
    private val apiClient = ApiClient()

    fun add(context: Context, audience: String) {
        apiClient.addAudience(context, audience)
    }

    fun delete(context: Context, audience: String) {
        apiClient.deleteAudience(context, audience)
    }
}