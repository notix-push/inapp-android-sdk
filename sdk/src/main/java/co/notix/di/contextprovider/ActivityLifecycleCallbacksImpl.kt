package co.notix.di.contextprovider

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class ActivityLifecycleCallbacksImpl(
    val onActivityShow: (activity: Activity) -> Unit,
    val onActivityGone: (activity: Activity) -> Unit
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        onActivityShow(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        onActivityGone(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        onActivityGone(activity)
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        onActivityGone(activity)
    }
}