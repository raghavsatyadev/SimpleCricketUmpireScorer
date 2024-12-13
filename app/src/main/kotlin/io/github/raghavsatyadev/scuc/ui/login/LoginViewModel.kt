package io.github.raghavsatyadev.scuc.ui.login

import androidx.lifecycle.viewModelScope
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.core.CoreViewModel
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.google.GoogleSignInUtil
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import io.github.raghavsatyadev.support.models.essential.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : CoreViewModel() {
    private var loginEvent: MutableStateFlow<Resource<Boolean>> = MutableStateFlow(Resource.empty())

    fun getLoginEvent() = loginEvent.asSharedFlow()

    fun signIn(
        signInUtil: GoogleSignInUtil,
    ) {
        viewModelScope.launch {
            loginEvent.emit(Resource.loading())
            withContext(ioDispatcher) {
                signInUtil.startSignIn(
                    onSuccess = { idToken ->
                        viewModelScope.launch {
                            withContext(ioDispatcher) {
                                val signInWithGoogle = FirebaseAuthUtil
                                    .getInstance()
                                    .signInWithGoogle(idToken)
                                val user = signInWithGoogle.first
                                if (user != null) {
                                    loginEvent.emit(Resource.success(true))
                                } else if (signInWithGoogle.second != null) {
                                    loginEvent.emit(Resource.error(signInWithGoogle.second!!))
                                } else {
                                    loginEvent.emit(Resource.error(null))
                                }
                            }
                        }
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
}