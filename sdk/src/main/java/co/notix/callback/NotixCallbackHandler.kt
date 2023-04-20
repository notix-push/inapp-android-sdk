package co.notix.callback

import android.content.Context

public interface NotixCallbackHandler {
    public fun handle(context: Context, callback: NotixCallback)
}