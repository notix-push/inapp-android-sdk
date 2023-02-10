package com.notix.notixsdk.services

import android.content.Context
import androidx.work.*
import com.notix.notixsdk.interstitial.ClosingSettings
import com.notix.notixsdk.interstitial.InterstitialButton
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.workers.InterstitialStartupWorker

class StartupService {

    private val storage = StorageProvider()

    fun interstitialStartup(
        context: Context, //Activity before
        customButton: InterstitialButton? = null,
        closingSettings: ClosingSettings? = null,
    ) {
        val interstitialStartupEnabled = storage.getInterstitialStartupEnabled(context)
        if (!interstitialStartupEnabled) {
            return
        }

        //TODO Network connected check
        val constraints: Constraints = Constraints.Builder()
            .build()

        val data = Data.Builder()

        if (customButton != null) {
            data.putString(InterstitialStartupWorker.NOTIX_WORKER_BUTTON_TEXT, customButton.text)
            data.putString(
                InterstitialStartupWorker.NOTIX_WORKER_BUTTON_TEXT_COLOR,
                customButton.textColor
            )
            data.putString(
                InterstitialStartupWorker.NOTIX_WORKER_BUTTON_BG_COLOR,
                customButton.backgroundColor
            )
        }

        if (closingSettings != null) {
            data.putInt(
                InterstitialStartupWorker.NOTIX_WORKER_CLOSE_TIMEOUT,
                closingSettings.timeout
            )
            data.putFloat(
                InterstitialStartupWorker.NOTIX_WORKER_CLOSE_OPACITY,
                closingSettings.opacity
            )
            data.putFloat(
                InterstitialStartupWorker.NOTIX_WORKER_CLOSE_SIZE,
                closingSettings.sizePercent
            )
        }

        val defaultZoneId = storage.getInterstitialDefaultZoneId(context)

        if (defaultZoneId != 0L) {
            data.putLong(InterstitialStartupWorker.NOTIX_WORKER_ZONE_ID, defaultZoneId)
        }

        val vars = storage.getInterstitialVars(context)

        data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_1, vars.var1)
        data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_2, vars.var2)
        data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_3, vars.var3)
        data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_4, vars.var4)
        data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_5, vars.var5)


        //TODO Check api version + send data
        val interstitialWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(InterstitialStartupWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data.build())
                .build()


        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                InterstitialStartupWorker.NOTIX_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                interstitialWorkRequest
            )

    }
}