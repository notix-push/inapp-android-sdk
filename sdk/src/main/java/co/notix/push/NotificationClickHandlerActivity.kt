package co.notix.push

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import co.notix.di.SingletonComponent
import co.notix.log.Logger
import co.notix.utils.getSelfLaunchIntent

internal class NotificationClickHandlerActivity : Activity() {
    private val reporter = SingletonComponent.eventReporter
    private val targetEventHandlerProvider = SingletonComponent.targetEventHandlerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.getStringExtra("click_data")?.let { reporter.reportClick(it) }
        val eventName = intent.getStringExtra("event")
        targetEventHandlerProvider.get()?.handle(this, eventName)
            ?: startMainActivity()
            ?: Logger.e("could not handle notification click")

        finish()
    }

    private fun startMainActivity() = getSelfLaunchIntent()?.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }?.let { runCatching { startActivity(it) }.getOrNull() }
}