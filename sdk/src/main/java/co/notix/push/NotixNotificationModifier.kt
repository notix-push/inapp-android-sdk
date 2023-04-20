package co.notix.push

import android.content.Context
import co.notix.log.Logger
import co.notix.data.Storage
import co.notix.push.data.NotificationParameters

public interface NotixNotificationModifier {
    /**
     * This function runs with a timeout of 1000ms. If the time is exceeded, a notification
     * will be displayed with the initial parameters.
     * @param context is application context
     * @param notificationParameters from incoming push
     * @return modified notificationParameters
     */
    public fun modify(
        context: Context,
        notificationParameters: NotificationParameters
    ): NotificationParameters
}

internal interface NotificationModifierProviderInitializer {
    fun init(notificationModifier: Class<out NotixNotificationModifier>?)
    fun init(notificationModifier: NotixNotificationModifier?)
}

internal interface NotificationModifierProvider {
    fun get(): NotixNotificationModifier?
}

internal class NotificationModifierProviderImpl(
    private val storage: Storage
) : NotificationModifierProvider, NotificationModifierProviderInitializer {
    private var notificationModifier: NotixNotificationModifier? = null

    override fun init(notificationModifier: Class<out NotixNotificationModifier>?) {
        val className = notificationModifier?.name ?: return
        tryToInstantiate(className) ?: return
        storage.setNotixNotificationModifierClass(className)
    }

    override fun init(notificationModifier: NotixNotificationModifier?) {
        this.notificationModifier = notificationModifier
    }

    override fun get(): NotixNotificationModifier? {
        if (notificationModifier != null) return notificationModifier
        notificationModifier = storage.getNotixNotificationModifierClass()
            ?.let { tryToInstantiate(it) }
        return notificationModifier
    }

    private fun tryToInstantiate(className: String) = runCatching {
        Class.forName(className).getConstructor()
            .newInstance() as NotixNotificationModifier
    }.onFailure {
        Logger.e(
            msg = "Unable to instantiate NotixNotificationModifier. Make sure that the provided class has a public no argument constructor. ${it.message}",
            throwable = it
        )
    }.getOrNull()
}