package com.notix.notixsdk.network

import kotlin.math.pow
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Response

internal suspend fun Call.awaitWithRetries(scope: CallAwaitWithRetriesScope.() -> Unit = {}): Response {
    val settings = CallAwaitWithRetriesScope().apply(scope)
    lateinit var throwable: Throwable
    repeat(settings.retryCount) { tryNumber ->
        if (tryNumber != 0) {
            delay(settings.retryDelay.calculateDelay(tryNumber - 1, settings.initialInterval))
        }
        runCatching {
            val response = this.clone().await()
            if (!settings.retryCondition.isSuccessful(response)) {
                response.close()
                error("condition ${settings.retryCondition.javaClass.simpleName} not met")
            }
            response
        }.fold(onSuccess = { return it }, onFailure = { throwable = it })
    }
    throw throwable
}

internal class CallAwaitWithRetriesScope {
    var retryCount: Int = 5
    var initialInterval: Long = 2000L
    var retryDelay: RetryDelay = RetryDelay.Exponential
    var retryCondition: RetryCondition = RetryCondition.BadStatusCode
}

internal sealed class RetryDelay {
    abstract fun calculateDelay(tryNumber: Int, initialInterval: Long): Long

    object Constant : RetryDelay() {
        override fun calculateDelay(tryNumber: Int, initialInterval: Long) = initialInterval
    }

    object Linear : RetryDelay() {
        override fun calculateDelay(tryNumber: Int, initialInterval: Long) =
            initialInterval * tryNumber
    }

    object Exponential : RetryDelay() {
        override fun calculateDelay(tryNumber: Int, initialInterval: Long) =
            initialInterval * 2.0.pow(tryNumber)
                .coerceAtMost(1000.0 * 60 * 60 * 12).toLong()
    }
}

internal sealed class RetryCondition {
    abstract fun isSuccessful(response: Response): Boolean

    object BadStatusCode : RetryCondition() {
        override fun isSuccessful(response: Response) = response.isSuccessful
    }

    class Custom internal constructor(val isSuccessfulInt: (response: Response) -> Boolean) :
        RetryCondition() {
        override fun isSuccessful(response: Response) = this.isSuccessfulInt(response)
    }
}

internal fun CallAwaitWithRetriesScope.customCondition(isSuccessful: (response: Response) -> Boolean) =
    RetryCondition.Custom(isSuccessful)