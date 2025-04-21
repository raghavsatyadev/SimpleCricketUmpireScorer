package io.github.raghavsatyadev.scus.compose.ui.user

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.components.UiStateManager
import io.github.raghavsatyadev.support.compose.components.withLoader
import io.github.raghavsatyadev.support.compose.google.FireStoreUtil
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.compose.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.extensions.AppExtensions
import io.github.raghavsatyadev.support.models.LoginState
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel
@Inject
constructor(
  private val authUtil: FirebaseAuthUtil,
  private val fireStoreUtil: FireStoreUtil,
  private val uiStateManager: UiStateManager,
) : CoreViewModel() {
  private val _loginResourceEvent = MutableStateFlow<Resource<LoginState>>(Resource.empty())
  val loginEvent =
    _loginResourceEvent
      .map { res ->
        when (res.status) {
          Resource.Status.SUCCESS -> res.data
          Resource.Status.ERROR -> LoginState.ERROR
          else -> null
        }
      }
      .stateIn(viewModelScope, SharingStarted.Lazily, null)

  fun signInWithGoogle(googleSignInUtil: GoogleSignInUtil) {
    uiStateManager.withLoader(viewModelScope) {
      withContext(ioDispatcher) {
        googleSignInUtil.startSignIn(
          onSuccess = { idToken -> signInWithFirebaseAuth(idToken) },
          onFailure = { e ->
            uiStateManager.withLoader(viewModelScope) {
              _loginResourceEvent.emit(Resource.error(CustomError(ErrorCode.AUTH_FAILED, e)))
            }
          },
        )
      }
    }
  }

  private fun signInWithFirebaseAuth(idToken: String) {
    uiStateManager.withLoader(viewModelScope) {
      withContext(ioDispatcher) {
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
    var remoteUser: User? = null
    try {
      remoteUser = util.getUser(user.userID)
    } catch (_: Exception) {}
    return if (remoteUser == null) {
      try {
        util.setUser(user)
        true
      } catch (e: Exception) {
        throw e
      }
    } else if (remoteUser.loginToken.isNullOrEmpty()) {
      try {
        util.updateUserLoginTokens()
        true
      } catch (e: Exception) {
        throw e
      }
    } else {
      _loginResourceEvent.emit(Resource.success(LoginState.USER_ALREADY_LOGGED_IN))
      false
    }
  }

  fun updateUserTokens() {
    uiStateManager.withLoader(viewModelScope) {
      _loginResourceEvent.emit(Resource.loading())
      withContext(ioDispatcher) {
        try {
          fireStoreUtil.updateUserLoginTokens()
          fireStoreUtil.initialize()
          _loginResourceEvent.emit(Resource.success(LoginState.SUCCESS))
        } catch (e: Exception) {
          _loginResourceEvent.emit(Resource.error(CustomError(ErrorCode.AUTH_FAILED, e)))
        }
      }
    }
  }

  fun signOut() {
    uiStateManager.withLoader(viewModelScope) {
      withContext(ioDispatcher) { AppExtensions.signOut() }
    }
  }
}
