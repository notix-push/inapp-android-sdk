package com.notix.notixsdk.push.firebase

import com.notix.notixsdk.NotixCallbackReporter
import com.notix.notixsdk.domain.NotixCallback
import com.notix.notixsdk.domain.NotixCallbackStatus
import com.notix.notixsdk.log.Logger

class FetchFcmTokenUseCase(
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
            NotixCallback.FcmTokenReceived(NotixCallbackStatus.FAILED, it.message)
        )
    }.getOrNull()
}