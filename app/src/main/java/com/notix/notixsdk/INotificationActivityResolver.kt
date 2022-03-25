package com.notix.notixsdk

import android.content.Intent

interface INotificationActivityResolver {
    fun resolveActivity(intent: Intent): Class<*>
}