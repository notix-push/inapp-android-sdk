package com.notix.notixsdk.domain


sealed class NotixCallback {
    abstract val status: NotixCallbackStatus
    abstract val data: String?

    data class Subscription(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class Unsubscription(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class Impression(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class Click(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class RefreshData(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class ManageAudience(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class PushDataLoad(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class ConfigLoaded(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class FcmTokenReceived(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class GeneralMetrics(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    data class AppInstall(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()
}
