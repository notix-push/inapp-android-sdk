package com.notix.notixsdk.perseverance.worker

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import com.notix.notixsdk.di.contextprovider.ContextProvider
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.perseverance.worker.SendEventsWorkerBackoff.BACKOFF_POLICY
import com.notix.notixsdk.perseverance.worker.SendEventsWorkerBackoff.TIME_UNIT
import com.notix.notixsdk.perseverance.worker.SendEventsWorkerBackoff.WORKER_INITIAL_BACKOFF_DELAY_MILLIS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SendEventsWorkerLauncher(
    private val contextProvider: ContextProvider,
    private val cs: CoroutineScope
) {
    fun launch() = cs.launch {
        runCatching {
            WorkManager.getInstance(contextProvider.appContext)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, buildWorkRequest())
                .await()
        }.fold(
            onSuccess = { Logger.v("worker enqueue $it") },
            onFailure = { Logger.e("worker enqueue error ${it.message}", it) }
        )
    }

    // every invocation of enqueueUniqueWork requires a different instance of WorkRequest
    private fun buildWorkRequest() = OneTimeWorkRequestBuilder<SendEventsWorker>()
        .setConstraints(CONSTRAINTS)
        .setBackoffCriteria(BACKOFF_POLICY, WORKER_INITIAL_BACKOFF_DELAY_MILLIS, TIME_UNIT)
        .build()

    companion object {
        private const val WORK_NAME = "send_events"
        private val CONSTRAINTS =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    }
}