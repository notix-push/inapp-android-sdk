package com.notix.notixsdk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.notix.notixsdk.di.SingletonComponent

class NotificationClickHandlerActivity : Activity() {
    private val reporter = SingletonComponent.eventReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultIntent = intent.extras?.get("notix-notification-intent")

        if (resultIntent != null && resultIntent is Intent) {
            intent.getStringExtra("click_data")?.let { reporter.reportClick(it) }
            startActivity(resultIntent)
        }

        finish()
    }
}