package com.lrs.dasparlament

import android.app.Activity
import android.content.res.Configuration
import androidx.core.view.WindowCompat

fun adjustStatusBarIconsToTheme(activity: Activity) {
    val window = activity.window
    val decorView = window.decorView
    val controller = WindowCompat.getInsetsController(window, decorView)

    val isDarkTheme = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    // Use light icons in dark mode, dark icons in light mode
    controller.isAppearanceLightStatusBars = !isDarkTheme
}
