@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.scus.compose.ui.user

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.raghavsatyadev.scus.R
import io.github.raghavsatyadev.support.compose.components.ErrorDialog
import io.github.raghavsatyadev.support.compose.components.LightPreview
import io.github.raghavsatyadev.support.compose.components.LightRealDevicePreview
import io.github.raghavsatyadev.support.compose.components.TransparentNavBar
import io.github.raghavsatyadev.support.compose.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.extensions.AppExtensions.activity
import io.github.raghavsatyadev.support.extensions.ImplicitIntentExtensions.openPlayStore
import io.github.raghavsatyadev.support.google.GoogleExtensions.checkPlayServiceAvailability
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

  LoginEvent(viewModel, onLoginSuccess, activity)
}

@Composable
private fun LoginEvent(
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
        activity?.finishAffinity()
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
            viewModel.signOut()
            activity?.finishAffinity()
            showUserAlreadyLoggedInDialog = false
          },
        )
      }
    }

    else -> {}
  }
}

@Composable
private fun CheckPlayService(content: @Composable () -> Unit) {
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
          Modifier.align(alignment = Alignment.BottomCenter)
            .padding(bottom = innerPadding.calculateBottomPadding() + 30.dp),
        onClick = { doLogin() },
      )
    }
  }
}

@Composable
private fun UserAlreadyLoggedInDialog(onForceLogin: () -> Unit, onSignOut: () -> Unit) {
  AlertDialog(
    confirmButton = {
      TextButton(onClick = { onForceLogin.invoke() }) { Text(stringResource(R.string.force_login)) }
    },
    dismissButton = {
      TextButton(onClick = { onSignOut.invoke() }) { Text(stringResource(R.string.logout)) }
    },
    title = { Text(stringResource(Rs.string.dialog_already_logged_in_title)) },
    text = { Text(AlreadyLoggedInText()) },
    onDismissRequest = {},
  )
}

@Composable
private fun AlreadyLoggedInText(): AnnotatedString {
  val title = stringResource(Rs.string.warning_already_logged_in_1)
  val bulletPoint1 = stringResource(Rs.string.warning_already_logged_in_2)
  val bulletPoint2 = stringResource(Rs.string.warning_already_logged_in_3)

  val annotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(fontSize = 16.sp)) { append(title) }
    append("\n")

    withStyle(style = ParagraphStyle(textIndent = TextIndent(firstLine = 14.sp))) {
      append("• $bulletPoint1\n")
    }
    withStyle(style = ParagraphStyle(textIndent = TextIndent(firstLine = 16.sp))) {
      append("• $bulletPoint2")
    }
  }
  return annotatedString
}

@LightRealDevicePreview()
// @DarkRealDevicePreview
@Composable
private fun PreviewLoginScreen() {
  LoginView(doLogin = {})
}

@LightPreview
@Composable
private fun UserAlreadyLoggedInDialogPreview() {
  UserAlreadyLoggedInDialog(onForceLogin = {}, onSignOut = {})
}
