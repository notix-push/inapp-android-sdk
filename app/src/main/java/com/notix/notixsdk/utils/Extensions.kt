package com.notix.notixsdk.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.browser.customtabs.CustomTabsService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

internal inline fun <reified T> JSONObject.getOrFallback(name: String, fallback: T): T =
    if (this.has(name)) (this.get(name) as? T) ?: fallback else fallback

fun CoroutineScope.safeLaunch(
    context: CoroutineContext = EmptyCoroutineContext,
    errorBlock: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit,
): Job {
    val errorHandler = CoroutineExceptionHandler { _, throwable ->
        errorBlock(throwable)
    }
    return launch(context + errorHandler, block = block)
}