@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.scus.compose.ui.user

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.compose.components.DarkRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.LightRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.TransparentNavBar
import io.github.raghavsatyadev.support.compose.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.openPlayStore
import io.github.raghavsatyadev.support.google.GoogleExtensions.checkPlayServiceAvailability
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun LoginScreen(viewModel: LoginScreenViewModel = hiltViewModel(), onLoginSuccess: () -> Unit) {
  if (viewModel.isLoggedIn()) {
    onLoginSuccess()
  } else {
    val loginEvent by viewModel.loginEvent.collectAsState()

    val activity = LocalActivity.current

    CheckPlayService {
      val googleSignInUtil = remember { GoogleSignInUtil(activity!!) }

      LaunchedEffect(loginEvent.status) {
        AppLog.loge(true, "LoginScreen", "LoginScreen", "loginEvent $loginEvent", Exception())
      }

      LoginView(doLogin = { viewModel.signInWithGoogle(googleSignInUtil) })
    }
  }
}

@Composable
fun CheckPlayService(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val isPlayServiceAvailable = context.checkPlayServiceAvailability()

  if (isPlayServiceAvailable) {
    content.invoke()
  } else {
    context.openPlayStore("com.google.android.gms")
  }
}

@Composable
private fun LoginView(doLogin: () -> Unit) {
  TransparentNavBar()

  Scaffold(modifier = Modifier) { innerPadding ->
    Box(modifier = Modifier) {
      Image(
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(Rs.drawable.img_background),
        contentDescription = stringResource(R.string.background),
      )
      SmallExtendedFloatingActionButton(
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        text = { Text(text = stringResource(R.string.google_login)) },
        icon = {
          Icon(
            tint = null,
            painter = painterResource(R.drawable.ic_google),
            contentDescription = stringResource(R.string.google_login),
          )
        },
        modifier =
          Modifier
            .align(alignment = Alignment.BottomCenter)
            .padding(bottom = innerPadding.calculateBottomPadding() + 30.dp),
        onClick = { doLogin() },
      )
    }
  }
}

@LightRealDevicePreview()
@DarkRealDevicePreview
@Composable
fun PreviewLoginScreen() {
  LoginView(doLogin = {})
}
