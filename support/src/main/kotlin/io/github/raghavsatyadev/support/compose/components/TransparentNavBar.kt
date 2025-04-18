package io.github.raghavsatyadev.support.compose.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun TransparentNavBar() {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    SideEffect {
        window?.let {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.isAppearanceLightNavigationBars = false
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
    }
}
