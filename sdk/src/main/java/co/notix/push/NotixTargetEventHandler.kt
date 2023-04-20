package co.notix.push

import android.content.Context
import co.notix.log.Logger
import co.notix.data.Storage

public interface NotixTargetEventHandler {
    /**
     * @param context is activity context
     * @param eventName is target event name
     */
    public fun handle(context: Context, eventName: String?)
}

internal interface TargetEventHandlerProviderInitializer {
    fun init(targetEventHandlerClass: Class<out NotixTargetEventHandler>?)
    fun init(targetEventHandler: NotixTargetEventHandler?)
}

internal interface TargetEventHandlerProvider {
    fun get(): NotixTargetEventHandler?
}

internal class TargetEventHandlerProviderImpl(
    private val storage: Storage
) : TargetEventHandlerProvider, TargetEventHandlerProviderInitializer {
    private var targetEventHandler: NotixTargetEventHandler? = null

    override fun init(targetEventHandlerClass: Class<out NotixTargetEventHandler>?) {
        val className = targetEventHandlerClass?.name ?: return
        tryToInstantiate(className) ?: return
        storage.setNotixTargetEventHandlerClass(className)
    }

    override fun init(targetEventHandler: NotixTargetEventHandler?) {
        this.targetEventHandler = targetEventHandler
    }

    override fun get(): NotixTargetEventHandler? {
        if (targetEventHandler != null) return targetEventHandler
        targetEventHandler = storage.getNotixTargetEventHandlerClass()?.let { tryToInstantiate(it) }
        return targetEventHandler
    }

    private fun tryToInstantiate(className: String) = runCatching {
        Class.forName(className).getConstructor()
            .newInstance() as NotixTargetEventHandler
    }.onFailure {
        Logger.e(
            msg = "Unable to instantiate NotixTargetEventHandler. Make sure that the provided class has a public no argument constructor. ${it.message}",
            throwable = it
        )
    }.getOrNull()
}