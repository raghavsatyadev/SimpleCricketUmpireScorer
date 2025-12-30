package io.github.raghavsatyadev.scus.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.github.raghavsatyadev.support.theme.AppTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent { AppTheme { MainScreen() } }
  }
}
