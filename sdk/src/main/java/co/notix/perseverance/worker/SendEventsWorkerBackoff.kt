package co.notix.perseverance.worker

import androidx.work.BackoffPolicy
import java.util.concurrent.TimeUnit
import kotlin.math.pow

internal object SendEventsWorkerBackoff {
    val TIME_UNIT = TimeUnit.MILLISECONDS
    val BACKOFF_POLICY = BackoffPolicy.EXPONENTIAL
    const val WORKER_INITIAL_BACKOFF_DELAY_MILLIS = 30_000L
    fun calculateBackoffMillisAsWorkManagerDoes(retryCount: Int) =
        (WORKER_INITIAL_BACKOFF_DELAY_MILLIS * 2.0.pow(retryCount).toLong())
            .coerceAtMost(TimeUnit.HOURS.toMillis(5L))
}