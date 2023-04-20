package co.notix.di

import co.notix.log.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob

internal class CoroutineScopesProvider {
    private val ceh = CoroutineExceptionHandler { _, throwable ->
        Logger.e(
            msg = "!NOTIX CoroutineExceptionHandler UNCAUGHT EXCEPTION!",
            throwable = throwable
        )
    }

    fun provideIo() = CoroutineScope(Dispatchers.IO + Job() + ceh)
    fun provideSupervisedIo() = CoroutineScope(Dispatchers.IO + SupervisorJob() + ceh)
}