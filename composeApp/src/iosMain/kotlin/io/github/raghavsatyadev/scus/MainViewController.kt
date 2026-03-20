package io.github.raghavsatyadev.scus

import androidx.compose.ui.window.ComposeUIViewController
import io.github.raghavsatyadev.library.support.theme.AppTheme
import io.github.raghavsatyadev.library.ui.main.MainScreen

fun MainViewController() = ComposeUIViewController { AppTheme { MainScreen {} } }

fun InitKoin() = io.github.raghavsatyadev.library.support.initKoin {}