package co.notix.push.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import co.notix.di.contextprovider.ContextProvider
import co.notix.utils.getTargetSdkVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class NotificationsPermissionController(
    private val contextProvider: ContextProvider
) {
    fun isPermissionNeeded() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.getTargetSdkVersion() >= Build.VERSION_CODES.TIRAMISU &&
                !isPermissionGranted()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isPermissionGranted() =
        ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    private val _canPostNotificationsF = MutableStateFlow<Boolean?>(null)
    val canPostNotificationsF = _canPostNotificationsF.asStateFlow()
    fun updateAndGetCanPostNotifications(): Boolean {
        val res = if (isPermissionNeeded()) isPermissionGranted() else true
        _canPostNotificationsF.value = res
        return res
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPermission(activity: Activity, requestCode: Int) =
        activity.requestPermissions(arrayOf(PERMISSION), requestCode)

    private val context
        get() = contextProvider.appContext
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private const val PERMISSION = Manifest.permission.POST_NOTIFICATIONS