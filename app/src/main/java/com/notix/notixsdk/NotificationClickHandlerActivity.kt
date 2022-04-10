package com.notix.notixsdk

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.notix.notixsdk.api.ApiClient

class NotificationClickHandlerActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultIntent = intent.extras?.get("notix-notification-intent")

        if (resultIntent != null && resultIntent is Intent) {
            val apiClient = ApiClient()
            val storage = StorageProvider()

            val clickData = intent.getStringExtra("click_data")

            apiClient.click(this, clickData)
            startActivity(resultIntent)

            val appId = storage.getAppId(this)
            val authToken = storage.getAuthToken(this)
            if (appId != null && authToken != null && appId != "" && authToken != "") {
                apiClient.refresh(this, appId, authToken)
            } else {
                Log.d("NotixDebug", "click refresh - invalid data. appId - $appId & authToken - $authToken")
            }

        }

        finish()
    }
}