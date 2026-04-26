@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.raghavsatyadev.library.ui.user

// import io.github.raghavsatyadev.support.components.TransparentNavBar // TODO migrate navigation
// bar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.raghavsatyadev.library.support.components.DarkRealDevicePreview
import io.github.raghavsatyadev.library.support.components.ErrorDialog
import io.github.raghavsatyadev.library.support.components.LightRealDevicePreview
import io.github.raghavsatyadev.library.support.models.essential.UiState
import io.github.raghavsatyadev.library.support.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import scus.composeapp.generated.resources.Res
import scus.composeapp.generated.resources.background
import scus.composeapp.generated.resources.google_login
import scus.composeapp.generated.resources.ic_google
import scus.composeapp.generated.resources.img_background

@Composable
fun LoginScreen(viewModel: LoginScreenViewModel = koinViewModel(), onLoginSuccess: () -> Unit) {
  val loginUiState by viewModel.isUserAlreadyLoggedInEvent.collectAsState()

  when (val state = loginUiState) {
    is UiState.Initial -> {}
    is UiState.Error -> {
      with(state.error) {
        ErrorDialog(errorCode = errorCode, errorMessage = exception?.message ?: errorCode.warning) {
          viewModel.signOut { /* activity?.finishAffinity() TODO migration */ }
        }
      }
    }
    is UiState.Success -> {
      if (state.data) {
        UserAlreadyLoggedInDialog(
          onForceLogin = { viewModel.updateUserTokens() },
          onSignOut = { viewModel.signOut { /* activity?.finishAffinity() TODO migration */ } },
        )
      } else {
        onLoginSuccess()
      }
    }
  }

  // TODO migrate login viewmodel and google sign in flow
  LoginView(doLogin = { /* TODO implement googleSignInUtil platform agnostic initiation */ })
}

@Composable
private fun LoginView(doLogin: () -> Unit) {
  // TransparentNavBar()

  Scaffold(modifier = Modifier) { innerPadding ->
    Box(modifier = Modifier) {
      Image(
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(Res.drawable.img_background),
        contentDescription = stringResource(Res.string.background),
      )
      SmallExtendedFloatingActionButton(
        contentColor = MaterialTheme.colorScheme.onSurface,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.extraLarge,
        text = { Text(text = stringResource(Res.string.google_login)) },
        icon = {
          Icon(
            tint = null, // To keep original colors
            painter = painterResource(Res.drawable.ic_google),
            contentDescription = stringResource(Res.string.google_login),
          )
        },
        modifier =
          Modifier.align(alignment = Alignment.BottomCenter)
            .padding(bottom = innerPadding.calculateBottomPadding() + 40.dp),
        onClick = doLogin,
      )
    }
  }
}

@LightRealDevicePreview
@DarkRealDevicePreview
@Composable
private fun PreviewLoginScreen() {
  AppTheme { LoginView(doLogin = {}) }
}
