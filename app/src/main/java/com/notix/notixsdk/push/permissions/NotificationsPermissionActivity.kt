package com.notix.notixsdk.push.permissions

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.log.Logger

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class NotificationsPermissionActivity : Activity() {
    private val controller = SingletonComponent.notificationsPermissionController

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            controller.updateAndGetCanPostNotifications()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller.requestPermission(this, PERMISSIONS_REQUEST_CODE)
    }

    override fun onStop() {
        super.onStop()
        controller.updateAndGetCanPostNotifications()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.updateAndGetCanPostNotifications()
    }
}

private const val PERMISSIONS_REQUEST_CODE = 71283