package io.github.raghavsatyadev.scus.ui.login

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.extensions.AppExtensions
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : CoreViewModel() {
    private var loginEvent: MutableStateFlow<Resource<LoginState>> =
        MutableStateFlow(Resource.empty())

    fun getLoginEvent() = loginEvent.asSharedFlow()

    fun signInWithGoogle(
        signInUtil: GoogleSignInUtil,
    ) {
        viewModelScope.launch {
            loginEvent.emit(Resource.loading())
            withContext(ioDispatcher) {
                signInUtil.startSignIn(
                    onSuccess = { idToken ->
                        signInWithFirebaseAuth(idToken)
                    },
                    onFailure = { exception ->
                        AppLog.loge(
                            false,
                            kotlinFileName,
                            "signIn",
                            exception,
                            Exception()
                        )
                        viewModelScope.launch {
                            loginEvent.emit(
                                Resource.error(
                                    CustomError(
                                        ErrorCode.AUTH_FAILED,
                                        exception
                                    )
                                )
                            )
                        }
                    })
            }
        }
    }

    fun signInWithFirebaseAuth(idToken: String) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val signInWithGoogle = FirebaseAuthUtil
                    .getInstance()
                    .signInWithGoogle(idToken)
                val user = signInWithGoogle.first
                if (user != null) {
                    try {
                        loginWithFirestore(user)
                    } catch (e: Exception) {
                        AppLog.loge(
                            false,
                            kotlinFileName,
                            "signIn",
                            e,
                            Exception()
                        )
                        loginEvent.emit(
                            Resource.error(
                                CustomError(
                                    ErrorCode.AUTH_FAILED,
                                    e
                                )
                            )
                        )
                    }
                } else if (signInWithGoogle.second != null) {
                    loginEvent.emit(Resource.error(signInWithGoogle.second!!))
                } else {
                    loginEvent.emit(Resource.error(null))
                }
            }
        }
    }

    @Throws(Exception::class)
    suspend fun loginWithFirestore(user: User) {
        with(FireStoreUtil.getInstance()) {
            try {
                val validateLoginToken = validateLoginToken(
                    this,
                    user
                )
                if (!validateLoginToken) {
                    return
                }
            } catch (e: Exception) {
                throw e
            }
            try {
                initialize()
                loginEvent.emit(Resource.success(LoginState.SUCCESS))
            } catch (e: Exception) {
                throw e
            }
        }
    }

    @Throws(Exception::class)
    suspend fun validateLoginToken(
        util: FireStoreUtil,
        user: User,
    ): Boolean {
        var remoteUser: User? = null
        try {
            remoteUser = util.getUser(user.userID)
        } catch (_: Exception) {

        }
        if (remoteUser == null) {
            try {
                util.setUser(user)
                return true
            } catch (e: Exception) {
                throw e
            }
        } else if (remoteUser.loginToken.isNullOrEmpty()) {
            try {
                util.updateUserLoginTokens()
                return true
            } catch (e: Exception) {
                throw e
            }
        } else {
            loginEvent.emit(Resource.success(LoginState.USER_ALREADY_LOGGED_IN))
            return false
        }
    }

    fun updateUserTokens() {
        viewModelScope.launch {
            loginEvent.emit(Resource.loading())
            withContext(ioDispatcher) {
                try {
                    val storeUtil = FireStoreUtil.getInstance()
                    storeUtil.updateUserLoginTokens()
                    storeUtil.initialize()
                    loginEvent.emit(Resource.success(LoginState.SUCCESS))
                } catch (e: Exception) {
                    AppLog.loge(
                        false,
                        kotlinFileName,
                        "updateUserTokens",
                        e,
                        Exception()
                    )
                    loginEvent.emit(
                        Resource.error(
                            CustomError(
                                ErrorCode.AUTH_FAILED,
                                e
                            )
                        )
                    )
                }
            }
        }
    }

    fun signOut(signInUtil: GoogleSignInUtil) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                AppExtensions.signOut()
            }
        }
    }
}

enum class LoginState {
    SUCCESS,
    USER_ALREADY_LOGGED_IN
}