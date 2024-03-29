package co.notix.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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

/// from androidx
internal fun Drawable.toBitmap(
    width: Int = intrinsicWidth,
    height: Int = intrinsicHeight,
    config: Bitmap.Config? = null
): Bitmap {
    if (this is BitmapDrawable) {
        if (config == null || bitmap.config == config) {
            // Fast-path to return original. Bitmap.createScaledBitmap will do this check, but it
            // involves allocation and two jumps into native code so we perform the check ourselves.
            if (width == bitmap.width && height == bitmap.height) {
                return bitmap
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    }

    val oldLeft = bounds.left
    val oldTop = bounds.top
    val oldRight = bounds.right
    val oldBottom = bounds.bottom

    val bitmap = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))

    setBounds(oldLeft, oldTop, oldRight, oldBottom)
    return bitmap
}