package io.github.raghavsatyadev.support.extensions

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Suppress("unused")
object OrientationExtensions {

    /**
     * Lock screen orientation to given value
     *
     * @param setPortrait if true, orientation will be locked to portrait, else
     *    landscape, default is portrait
     */
    fun Activity.lockOrientation(setPortrait: Boolean = true) {
        requestedOrientation =
            if (setPortrait) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    /** Sets orientation to [ActivityInfo.SCREEN_ORIENTATION_SENSOR] */
    fun Activity.unlockOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    /** check if current orientation is portrait */
    fun Activity.isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /** set orientation dependent on sensor of device */
    fun Activity.setFreeOrientation() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    fun Activity.enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controllerCompat = WindowInsetsControllerCompat(window, window.decorView)

        controllerCompat.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controllerCompat.hide(WindowInsetsCompat.Type.systemBars())

        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    fun Activity.disableFullScreen() {
        val controllerCompat = WindowInsetsControllerCompat(window, window.decorView)

        controllerCompat.show(WindowInsetsCompat.Type.systemBars())
    }
}