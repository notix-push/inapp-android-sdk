package co.notix.callback

import co.notix.di.contextprovider.ContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class NotixCallbackReporter(
    private val contextProvider: ContextProvider
) {
    // not using Notix internal coroutine scopes here because
    // we do not care if any exceptions happen in handler
    private val cs = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var handler: NotixCallbackHandler? = null

     internal fun report(callback: NotixCallback) {
        cs.launch {
            handler?.handle(contextProvider.appContext, callback)
        }
    }

    internal fun setHandler(handler: NotixCallbackHandler?) {
        this.handler = handler
    }
}