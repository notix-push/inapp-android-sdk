package co.notix.push.firebase

import co.notix.callback.NotixCallbackReporter
import co.notix.callback.NotixCallback
import co.notix.callback.NotixCallbackStatus
import co.notix.log.Logger

internal class FetchFcmTokenUseCase(
    private val firebaseInstanceProvider: FirebaseInstanceProvider,
    private val notixCallbackReporter: NotixCallbackReporter
) {
    suspend operator fun invoke() = runCatching {
        val fcm = firebaseInstanceProvider.getFcm() ?: error("fcm not initialized")
        fcm.token.await()
    }.onSuccess {
        Logger.v("FCM token fetched: $it")
        notixCallbackReporter.report(
            NotixCallback.FcmTokenReceived(NotixCallbackStatus.SUCCESS, it)
        )
    }.onFailure {
        Logger.e("Fetching FCM token failed ${it.message}")
        notixCallbackReporter.report(
            NotixCallback.FcmTokenReceived(NotixCallbackStatus.FAILURE, it.message)
        )
    }.getOrNull()
}