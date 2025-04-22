package io.github.raghavsatyadev.scus.compose.ui.user

import androidx.lifecycle.viewModelScope
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
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel
@Inject
constructor(
  private val authUtil: FirebaseAuthUtil,
  private val fireStoreUtil: FireStoreUtil,
  uiStateManager: UiStateManager,
) : CoreScreenViewModel(uiStateManager) {
  private val _loginResourceEvent = MutableStateFlow<Resource<LoginState>>(Resource.empty())
  val loginEvent =
    _loginResourceEvent
      .map { res ->
        when (res.status) {
          Resource.Status.SUCCESS -> {
            res.data
          }
          Resource.Status.ERROR -> {
            LoginState.ERROR
          }
          else -> {
            null
          }
        }
      }
      .stateIn(viewModelScope, SharingStarted.Eagerly, null)

  fun signInWithGoogle(googleSignInUtil: GoogleSignInUtil) {
    executeWithLoader {
      googleSignInUtil.startSignIn(
        onSuccess = { idToken -> signInWithFirebaseAuth(idToken) },
        onFailure = { e ->
          _loginResourceEvent.emit(Resource.error(CustomError(ErrorCode.AUTH_FAILED, e)))
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
        _loginResourceEvent.emit(Resource.error(CustomError(ErrorCode.AUTH_FAILED, e)))
      }
    } else if (error != null) {
      _loginResourceEvent.emit(Resource.error(error))
    } else {
      _loginResourceEvent.emit(Resource.error(null))
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
        _loginResourceEvent.emit(Resource.success(LoginState.SUCCESS))
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
        _loginResourceEvent.emit(Resource.success(LoginState.USER_ALREADY_LOGGED_IN))
        false
      }
    }
  }

  fun updateUserTokens() {
    executeWithLoader {
      try {
        fireStoreUtil.updateUserLoginTokens()
        fireStoreUtil.initialize()
        _loginResourceEvent.emit(Resource.success(LoginState.SUCCESS))
      } catch (e: Exception) {
        _loginResourceEvent.emit(Resource.error(CustomError(ErrorCode.AUTH_FAILED, e)))
      }
    }
  }

  fun signOut(onSuccess: () -> Unit) {
    executeWithLoader {
      AppComposeExtensions.signOut(fireStoreUtil, authUtil)
      onSuccess()
    }
  }
}
