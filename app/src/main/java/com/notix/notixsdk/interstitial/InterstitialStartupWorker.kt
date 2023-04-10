package com.notix.notixsdk.interstitial

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.notix.notixsdk.NotixInterstitial
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.providers.StorageProvider

class InterstitialStartupWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    companion object {
        const val NOTIX_WORKER_NAME = "NOTIX_INTERSTITIAL_WORKER"

        const val NOTIX_WORKER_BUTTON_TEXT = "NOTIX_WORKER_BUTTON_TEXT"
        const val NOTIX_WORKER_BUTTON_TEXT_COLOR = "NOTIX_WORKER_BUTTON_TEXT_COLOR"
        const val NOTIX_WORKER_BUTTON_BG_COLOR = "NOTIX_WORKER_BUTTON_BG_COLOR"

        const val NOTIX_WORKER_CLOSE_TIMEOUT = "NOTIX_WORKER_CLOSE_TIMEOUT"
        const val NOTIX_WORKER_CLOSE_OPACITY = "NOTIX_WORKER_CLOSE_OPACITY"
        const val NOTIX_WORKER_CLOSE_SIZE = "NOTIX_WORKER_CLOSE_SIZE"

        const val NOTIX_WORKER_ZONE_ID = "NOTIX_WORKER_ZONE_ID"

        const val NOTIX_WORKER_VAR_1 = "NOTIX_WORKER_VAR_1"
        const val NOTIX_WORKER_VAR_2 = "NOTIX_WORKER_VAR_2"
        const val NOTIX_WORKER_VAR_3 = "NOTIX_WORKER_VAR_3"
        const val NOTIX_WORKER_VAR_4 = "NOTIX_WORKER_VAR_4"
        const val NOTIX_WORKER_VAR_5 = "NOTIX_WORKER_VAR_5"
    }

    override fun doWork(): Result {
        Logger.i("Interstitial startup worker run")

        val buttonText = inputData.getString(NOTIX_WORKER_BUTTON_TEXT) ?: "OPEN"
        val buttonTextColor = inputData.getString(NOTIX_WORKER_BUTTON_TEXT_COLOR) ?: "#ffffff"
        val buttonTextBgColor = inputData.getString(NOTIX_WORKER_BUTTON_BG_COLOR) ?: "#eb4511"

        val interstitial = NotixInterstitial.build(null) {
            customButtons = listOf(
                InterstitialButton(
                    text = buttonText,
                    textColor = buttonTextColor,
                    backgroundColor = buttonTextBgColor,
                )
            )
            closingSettings = ClosingSettings(
                timeout = inputData.getInt(NOTIX_WORKER_CLOSE_TIMEOUT, 3),
                opacity = inputData.getFloat(NOTIX_WORKER_CLOSE_OPACITY, 0.7f),
                sizePercent = inputData.getFloat(NOTIX_WORKER_CLOSE_SIZE, 1f),
            )
        }

        var zoneId = inputData.getLong(NOTIX_WORKER_ZONE_ID, 0)

        if (zoneId == 0L) {
            zoneId = StorageProvider().getInterstitialLastZoneId(applicationContext)
        }

        interstitial.load(
            applicationContext,
            zoneId,
            vars = DomainModels.RequestVars(
                var1 = inputData.getString(NOTIX_WORKER_VAR_1) ?: "",
                var2 = inputData.getString(NOTIX_WORKER_VAR_2) ?: "",
                var3 = inputData.getString(NOTIX_WORKER_VAR_3) ?: "",
                var4 = inputData.getString(NOTIX_WORKER_VAR_4) ?: "",
                var5 = inputData.getString(NOTIX_WORKER_VAR_5) ?: "",
            ),
            onLoadCallback = {}
        )
        interstitial.show(applicationContext)

        return Result.success()
    }
}