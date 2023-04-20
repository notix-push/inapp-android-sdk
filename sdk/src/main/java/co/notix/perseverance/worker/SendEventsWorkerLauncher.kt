package co.notix.perseverance.worker

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.await
import co.notix.di.contextprovider.ContextProvider
import co.notix.log.Logger
import co.notix.perseverance.worker.SendEventsWorkerBackoff.BACKOFF_POLICY
import co.notix.perseverance.worker.SendEventsWorkerBackoff.TIME_UNIT
import co.notix.perseverance.worker.SendEventsWorkerBackoff.WORKER_INITIAL_BACKOFF_DELAY_MILLIS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class SendEventsWorkerLauncher(
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