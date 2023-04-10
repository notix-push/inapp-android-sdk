package com.notix.notixsdk

import com.notix.notixsdk.domain.NotixCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotixCallbackReporter {
    // not using Notix internal coroutine scopes here because
    // we do not care if any exceptions happen in handler
    private val cs = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var handler: ((NotixCallback) -> Unit)? = null

    fun report(status: NotixCallback) = cs.launch {
        handler?.invoke(status)
    }

    fun setHandler(handler: ((NotixCallback) -> Unit)?) {
        this.handler = handler
    }
}