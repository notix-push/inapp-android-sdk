package com.notix.notixsdk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.notix.notixsdk.api.ApiClient

class NotificationClickHandlerActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultIntent = intent.extras?.get("notix-notification-intent")

        if (resultIntent != null && resultIntent is Intent) {
            val clickData = intent.getStringExtra("click_data")

            ApiClient().click(this, clickData)
            startActivity(resultIntent)
        }

        finish()
    }
}