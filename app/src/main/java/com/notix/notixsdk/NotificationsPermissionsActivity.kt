package com.notix.notixsdk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.notix.notixsdk.providers.StorageProvider

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
} else {
    arrayOf()
}

class NotificationsPermissionsActivity : Activity() {
    private val storage = StorageProvider()

    companion object {
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            Log.d("NotixDebug", "permissions finishied")
            storage.setNotificationsPermissionsAllowed(this, hasPermissions(this))
        }

        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermissions(this)) {
                requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
            } else {
                Log.d("NotixDebug", "has permission")
                storage.setNotificationsPermissionsAllowed(this, hasPermissions(this))
                finish()
            }
        } else {
            Log.d("NotixDebug", "lower tiramissu")
            storage.setNotificationsPermissionsAllowed(this, true)
            finish()
        }
    }
}