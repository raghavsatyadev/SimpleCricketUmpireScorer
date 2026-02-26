package io.github.raghavsatyadev.library.ui.user

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.library.support.components.UiStateManager
import io.github.raghavsatyadev.library.support.core.CoreScreenViewModel
import io.github.raghavsatyadev.library.support.google.repository.AuthRepository
import io.github.raghavsatyadev.library.support.google.repository.LoginTokenStatus
import io.github.raghavsatyadev.library.support.models.essential.CustomError
import io.github.raghavsatyadev.library.support.models.essential.ErrorCode
import io.github.raghavsatyadev.library.support.models.essential.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel(
  uiStateManager: UiStateManager,
  private val authRepository: AuthRepository,
) : CoreScreenViewModel(uiStateManager) {
  private val _isUserAlreadyLoggedInEvent = MutableStateFlow<UiState<Boolean>>(UiState.Initial)
  @Stable val isUserAlreadyLoggedInEvent = _isUserAlreadyLoggedInEvent.asStateFlow()

  fun initiateGoogleLogin(
    googleSignInUtil: Any // TODO: Replace with generic KMP sign-in util or remove if not needed in
    // common
  ) {
    // googleSignInUtil is Android specific. We need a KMP abstraction.
    // For now, we will keep the method signature but abstract it if possible.
  }

  fun signInWithGoogle(idToken: String) {
    executeWithLoader { signInWithFirebaseAuth(idToken) }
  }

  fun onSignInError(e: Exception) {
    viewModelScope.launch {
      _isUserAlreadyLoggedInEvent.emit(UiState.Error(CustomError(ErrorCode.AUTH_FAILED, e)))
    }
  }

  private suspend fun signInWithFirebaseAuth(idToken: String) {
    val (user, error) = authRepository.signInWithGoogle(idToken)
    if (user != null) {
      try {
        when (val result = authRepository.validateLoginToken(user)) {
          is LoginTokenStatus.Success -> _isUserAlreadyLoggedInEvent.emit(UiState.Success(false))
          is LoginTokenStatus.Error ->
            _isUserAlreadyLoggedInEvent.emit(
              UiState.Error(CustomError(ErrorCode.AUTH_FAILED, result.exception))
            )
          is LoginTokenStatus.RemoteTokenMismatch ->
            _isUserAlreadyLoggedInEvent.emit(UiState.Success(true))
        }
      } catch (e: Exception) {
        _isUserAlreadyLoggedInEvent.emit(UiState.Error(CustomError(ErrorCode.AUTH_FAILED, e)))
      }
    } else if (error != null) {
      _isUserAlreadyLoggedInEvent.emit(UiState.Error(error))
    }
  }

  fun updateUserTokens() {
    executeWithLoader {
      try {
        authRepository.updateUserTokens()
        _isUserAlreadyLoggedInEvent.emit(UiState.Success(false))
      } catch (e: Exception) {
        _isUserAlreadyLoggedInEvent.emit(UiState.Error(CustomError(ErrorCode.AUTH_FAILED, e)))
      }
    }
  }

  fun signOut(onLogout: () -> Unit) {
    executeWithLoader {
      authRepository.signOut()
      onLogout()
    }
  }

  fun loginEventConsumed() {
    _isUserAlreadyLoggedInEvent.value = UiState.Initial
  }
}
