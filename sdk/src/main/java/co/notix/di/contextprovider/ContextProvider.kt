package co.notix.di.contextprovider

import android.app.Activity
import android.app.Application
import android.content.Context
import co.notix.log.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ContextProviderImpl : ContextProvider, ContextProviderInitializer {
    override lateinit var appContext: Context
        private set
    override lateinit var application: Application
        private set
    private val _currentActivity = MutableStateFlow<Activity?>(null)
    override val currentActivity: StateFlow<Activity?> = _currentActivity.asStateFlow()

    override fun tryInit(context: Context) {
        if (this::appContext.isInitialized) return
        appContext = context.applicationContext
        application = appContext as Application
        application.registerActivityLifecycleCallbacks(
            ActivityLifecycleCallbacksImpl(
                onActivityShow = { activity ->
                    Logger.v("activity +show+ $activity")
                    _currentActivity.value = activity
                },
                onActivityGone = { activity ->
                    if (_currentActivity.value == activity) {
                        _currentActivity.value = null
                        Logger.v("activity -gone- $activity")
                    }
                }
            )
        )
    }
}

internal interface ContextProvider {
    val appContext: Context
    val application: Application
    val currentActivity: StateFlow<Activity?>
}

internal interface ContextProviderInitializer {
    fun tryInit(context: Context)
}