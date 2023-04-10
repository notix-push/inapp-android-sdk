package com.notix.notixsdk.log

import android.util.Log
import com.notix.notixsdk.log.LogLevel.IMPORTANT
import com.notix.notixsdk.log.LogLevel.FULL

// ERROR > WARN > INFO > DEBUG > VERBOSE
object Logger {
    var logLevel = LogLevel.NONE

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

const val TAG = "NotixLog"