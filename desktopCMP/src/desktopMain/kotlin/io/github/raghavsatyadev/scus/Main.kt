package io.github.raghavsatyadev.scus

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.raghavsatyadev.library.support.theme.AppTheme

fun main() {
  application { Window(onCloseRequest = ::exitApplication, title = "KMP App") { AppTheme {} } }
}
