package co.notix.log

import android.util.Log
import co.notix.log.LogLevel.FULL
import co.notix.log.LogLevel.IMPORTANT

// ERROR > WARN > INFO > DEBUG > VERBOSE
internal object Logger {
    var logLevel = IMPORTANT

    /**
     * Errors, printed when logLevel is either [IMPORTANT] or [FULL]
     */
    fun e(msg: String? = null, throwable: Throwable? = null, tag: String = TAG) {
        if (logLevel == FULL || logLevel == IMPORTANT) {
            Log.e(tag, msg, throwable)
        }
    }

    /**
     * Info, printed when logLevel is either [IMPORTANT] or [FULL]
     */
    fun i(msg: String, tag: String = TAG) {
        if (logLevel == FULL || logLevel == IMPORTANT) {
            Log.i(tag, msg)
        }
    }

    /**
     * Verbose, printed when logLevel is [FULL]
     */
    fun v(msg: String, tag: String = TAG) {
        if (logLevel == FULL) {
            Log.v(tag, msg)
        }
    }
}

internal const val TAG = "NotixLog"