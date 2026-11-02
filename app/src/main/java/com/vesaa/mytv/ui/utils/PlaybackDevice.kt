package com.vesaa.mytv.ui.utils

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.res.Configuration

fun Context.isTelevision(): Boolean =
    (getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager)?.currentModeType ==
        Configuration.UI_MODE_TYPE_TELEVISION ||
        packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> if (baseContext !== this) baseContext.findActivity() else null
    else -> null
}
