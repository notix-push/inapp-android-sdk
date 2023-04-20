package co.notix.callback

public sealed class NotixCallback {
    public abstract val status: NotixCallbackStatus
    public abstract val data: String?

    public data class Subscription(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class Unsubscription(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class Impression(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class Click(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class RefreshData(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class ManageAudience(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class PushDataLoad(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class ConfigLoaded(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class FcmTokenReceived(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class GeneralMetrics(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()

    public data class AppInstall(
        override val status: NotixCallbackStatus,
        override val data: String?
    ) : NotixCallback()
}
