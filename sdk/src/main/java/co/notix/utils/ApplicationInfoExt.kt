package co.notix.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

internal fun Context.getTargetSdkVersion() = runCatching {
    packageManager.getApplicationInfo(packageName, 0).targetSdkVersion
}.getOrDefault(Build.VERSION_CODES.KITKAT)

internal fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
    }

internal fun Context.getSelfLaunchIntent() = packageManager.getLaunchIntentForPackage(packageName)