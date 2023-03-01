package com.notix.notixsdk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.providers.NotixFirebaseInitProvider
import com.notix.notixsdk.providers.StorageProvider

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
} else {
    arrayOf()
}

class NotificationsPermissionsActivity : Activity() {
    private val storage = StorageProvider()
    private val apiClient = ApiClient()
    private var notixFirebaseInitProvider: NotixFirebaseInitProvider? = null

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
            if (hasPermissions(this)) {
                initPushNotifications()
            }
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
                initPushNotifications()
                finish()
            }
        } else {
            Log.d("NotixDebug", "lower tiramissu")
            storage.setNotificationsPermissionsAllowed(this, true)
            initPushNotifications()
            finish()
        }
    }

    private fun initPushNotifications() {
        val appId = intent.getStringExtra("app_id")
        val authToken = intent.getStringExtra("auth_token")

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "App id cannot be null or empty")
            return
        }
        if (authToken.isNullOrEmpty()) {
            Log.d("NotixDebug", "Auth token cannot be null or empty")
            return
        }

        val initFirebaseProvider = {
            storage.setPackageName(this, this.packageName)
            storage.setAuthToken(this, authToken)
            storage.getUUID(this)

            notixFirebaseInitProvider = NotixFirebaseInitProvider()
            notixFirebaseInitProvider!!.init(this)
        }

        apiClient.getConfig(
            context = this,
            appId = appId,
            authToken = authToken,
            getConfigDoneCallback = initFirebaseProvider,
        )
    }
}