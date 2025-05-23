package io.github.raghavsatyadev.scus.compose.ui.user

import androidx.compose.runtime.Stable
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.AppComposeExtensions
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.core.CoreScreenViewModel
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.compose.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.models.LoginState
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.models.essential.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel
@Inject
constructor(
  private val authUtil: FirebaseAuthUtil,
  private val fireStoreUtil: FireStoreUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  private val _loginResourceEvent = MutableStateFlow<UiState<LoginState>>(UiState.Initial)
  @Stable
  val loginEvent = _loginResourceEvent.asStateFlow()

  fun signInWithGoogle(googleSignInUtil: GoogleSignInUtil) {
    executeWithLoader {
      googleSignInUtil.startSignIn(
        onSuccess = { idToken -> signInWithFirebaseAuth(idToken) },
        onFailure = { e ->
          _loginResourceEvent.emit(
            UiState.Error(
              CustomError(
                ErrorCode.AUTH_FAILED,
                e
              )
            )
          )
        },
      )
    }
  }

  private suspend fun signInWithFirebaseAuth(idToken: String) {
    val (user, error) = authUtil.signInWithGoogle(idToken)
    if (user != null) {
      try {
        loginWithFirestore(user)
      } catch (e: Exception) {
        _loginResourceEvent.emit(
          UiState.Error(
            CustomError(
              ErrorCode.AUTH_FAILED,
              e
            )
          )
        )
      }
    } else {
      _loginResourceEvent.emit(UiState.Error(error))
    }
  }

  @Throws(Exception::class)
  private suspend fun loginWithFirestore(user: User) {
    with(fireStoreUtil) {
      try {

        if (!validateLoginToken(this, user)) {
          return
        }
        initialize()
        _loginResourceEvent.emit(UiState.Success(LoginState.SUCCESS))
      } catch (e: Exception) {
        throw e
      }
    }
  }

  @Throws(Exception::class)
  private suspend fun validateLoginToken(util: FireStoreUtil, user: User): Boolean {
    val remoteUser =
      try {
        util.getUser(user.userID)
      } catch (_: Exception) {
        null
      }

    return when {
      remoteUser == null -> {
        try {
          util.setUser(user)
        } catch (e: Exception) {
          throw e
        }
        true
      }
      remoteUser.loginToken.isNullOrEmpty() -> {
        util.updateUserLoginTokens()
        true
      }
      else -> {
        _loginResourceEvent.emit(UiState.Success(LoginState.USER_ALREADY_LOGGED_IN))
        false
      }
    }
  }

  fun updateUserTokens() {
    executeWithLoader {
      try {
        fireStoreUtil.updateUserLoginTokens()
        fireStoreUtil.initialize()
        _loginResourceEvent.emit(UiState.Success(LoginState.SUCCESS))
      } catch (e: Exception) {
        _loginResourceEvent.emit(
          UiState.Error(
            CustomError(
              ErrorCode.AUTH_FAILED,
              e
            )
          )
        )
      }
    }
  }

    fun signOut(onLogout: () -> Unit) {
    executeWithLoader {
      AppComposeExtensions.signOut(fireStoreUtil, authUtil)
        onLogout() // Trigger navigation to login screen
    }
  }

  /**
   * Call this method after the login success event has been handled by the
   * UI to prevent it from being processed again.
   */
  fun onLoginSuccessEventHandled() {
    if (_loginResourceEvent.value == UiState.Success) {
      if ((_loginResourceEvent.value as UiState.Success).data == LoginState.SUCCESS) {
        _loginResourceEvent.value = UiState.Initial
      }
    }
  }
}
