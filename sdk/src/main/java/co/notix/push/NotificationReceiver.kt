package co.notix.push

import android.app.ActivityManager
import android.content.Context
import android.widget.Toast
import co.notix.di.contextprovider.ContextProvider
import co.notix.perseverance.WorkerDataStore
import co.notix.perseverance.addCommand
import co.notix.perseverance.command.ShowNotificationCommand
import co.notix.perseverance.worker.SendEventsWorkerLauncher
import co.notix.push.data.Notification
import co.notix.push.permissions.NotificationsPermissionController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal interface NotificationReceiver {
    fun receive(notification: Notification)
}

internal class NotificationReceiverImpl(
    private val notificationsPermissionController: NotificationsPermissionController,
    private val contextProvider: ContextProvider,
    private val workerDataStore: WorkerDataStore,
    private val workerLauncher: SendEventsWorkerLauncher,
    private val csIo: CoroutineScope
) : NotificationReceiver {
    override fun receive(notification: Notification) {
        if (!notificationsPermissionController.updateAndGetCanPostNotifications()) return

        if (isAppOnForeground(context) &&
            notification.notifParamsInt.targetUrlData != null &&
            notification.notifParams.showToast &&
            notification.notifParams.text != null
        ) {
            showToast(context, notification.notifParams.text)
        }

        csIo.launch {
            workerDataStore.addCommand(
                ShowNotificationCommand,
                ShowNotificationCommand.Params(notification)
            )
            workerLauncher.launch()
        }
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun showToast(context: Context, text: String) =
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }

    private val context
        get() = contextProvider.appContext
}