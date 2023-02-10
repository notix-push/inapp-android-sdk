package com.notix.notixsdk.utils

import android.content.Context
import com.notix.notixsdk.providers.StorageProvider

internal fun permissionsAllowed(context: Context): Boolean {
    return StorageProvider().getNotificationsPermissionsAllowed(context)
}