@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.scus.compose.ui.user

import android.app.Activity
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.AppComposeExtensions.CheckPlayService
import io.github.raghavsatyadev.support.compose.AppComposeExtensions.activity
import io.github.raghavsatyadev.support.compose.components.DarkRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.ErrorDialog
import io.github.raghavsatyadev.support.compose.components.LightRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.TransparentNavBar
import io.github.raghavsatyadev.support.compose.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.models.LoginState
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.R as Rs

@Composable
fun LoginScreen(viewModel: LoginScreenViewModel = hiltViewModel(), onLoginSuccess: () -> Unit) {

  val activity = activity()

  CheckPlayService {
    val googleSignInUtil = remember { GoogleSignInUtil(activity = activity!!) }

    LoginView(doLogin = { viewModel.signInWithGoogle(googleSignInUtil) })
  }

  AfterLogin(viewModel, onLoginSuccess, activity)
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
          Modifier.align(alignment = Alignment.BottomCenter)
            .padding(bottom = innerPadding.calculateBottomPadding() + 30.dp),
        onClick = { doLogin() },
      )
    }
  }
}

@Composable
private fun AfterLogin(
  viewModel: LoginScreenViewModel,
  onLoginSuccess: () -> Unit,
  activity: Activity?,
) {
  val loginEvent by viewModel.loginEvent.collectAsState()
  var showUserAlreadyLoggedInDialog by remember { mutableStateOf(true) }

  when (loginEvent) {
    LoginState.SUCCESS -> {
      onLoginSuccess()
    }

    LoginState.ERROR -> {
      ErrorDialog(
        errorCode = ErrorCode.UNKNOWN_ERROR,
        errorMessage = stringResource(Rs.string.warning_unknown_error),
      ) {
        viewModel.signOut { activity?.finishAffinity() }
      }
    }

    LoginState.USER_ALREADY_LOGGED_IN -> {
      if (showUserAlreadyLoggedInDialog) {
        UserAlreadyLoggedInDialog(
          onForceLogin = {
            viewModel.updateUserTokens()
            showUserAlreadyLoggedInDialog = false
          },
          onSignOut = {
            viewModel.signOut {
              activity?.finishAffinity()
              showUserAlreadyLoggedInDialog = false
            }
          },
        )
      }
    }

    else -> {}
  }
}

@LightRealDevicePreview
@DarkRealDevicePreview
@Composable
private fun PreviewLoginScreen() {
  LoginView(doLogin = {})
}
