package com.notix.notixsdk.services

import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.providers.StorageProvider
import kotlinx.coroutines.launch

class MetricsService {
    private val storage = StorageProvider()
    private val metricsRepository = SingletonComponent.metricsRepository
    private val csIo = SingletonComponent.csProvider.provideSupervisedIo()

    fun onLaunch() = csIo.launch {
        val runCount = (storage.getRunCount() ?: 0) + 1
        storage.setRunCount(runCount)
        if (runCount == 1L) {
            metricsRepository.sendAppInstall()
        }
        metricsRepository.sendGeneralMetrics()
    }
}