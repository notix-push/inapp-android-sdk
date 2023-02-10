package com.notix.notixsdk.services

import android.content.Context
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.api.MetricsClient

class MetricsService {
    private val client = MetricsClient()
    private val storage = StorageProvider()

    fun doGeneralMetric(context: Context) {
        client.trackGeneralMetric(context)
    }

    fun incrementRunCount(context: Context) {
        var currentValue = storage.getRunCount(context)
        if (currentValue == null) {
            currentValue = 1
        } else {
            currentValue += 1
        }

        storage.setRunCount(context, currentValue)
    }
}