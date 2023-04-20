package co.notix.services

import co.notix.di.SingletonComponent
import co.notix.data.Storage
import kotlinx.coroutines.launch

internal class MetricsService {
    private val storage = Storage()
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