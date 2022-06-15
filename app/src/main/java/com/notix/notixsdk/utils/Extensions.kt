package com.notix.notixsdk.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabsService

internal fun Context.getBrowserIntentPackages(): List<ResolveInfo> {
    // Get default VIEW intent handler.
    val activityIntent = Intent()
        .setAction(Intent.ACTION_VIEW)
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setData(Uri.fromParts("http", "", null))

    // Get all apps that can handle VIEW intents.
    return packageManager.queryIntentActivities(activityIntent, 0)
}

internal fun Context.canHandleBrowserIntent() = getBrowserIntentPackages().isNotEmpty()

internal fun Context.hasCustomTabsBrowser(): Boolean {
    val resolvedActivityList = getBrowserIntentPackages()
    val packagesSupportingCustomTabs = mutableListOf<ResolveInfo>()
    for (info in resolvedActivityList) {
        val serviceIntent = Intent()
        serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
        serviceIntent.setPackage(info.activityInfo.packageName)
        // Check if this package also resolves the Custom Tabs service.
        if (packageManager.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(info)
        }
    }
    return packagesSupportingCustomTabs.isNotEmpty()
}