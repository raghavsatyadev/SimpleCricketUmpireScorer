package io.github.raghavsatyadev.scus

import androidx.compose.ui.window.ComposeUIViewController
import io.github.raghavsatyadev.library.support.initKoin
import io.github.raghavsatyadev.library.support.theme.AppRoot
import io.github.raghavsatyadev.library.ui.main.MainScreen

fun MainViewController() = ComposeUIViewController { AppRoot { MainScreen() } }

fun InitKoin() = initKoin {}
