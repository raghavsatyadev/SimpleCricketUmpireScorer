package io.github.raghavsatyadev.scus.compose.ui.user

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.core.CoreViewModel
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel
@Inject
constructor(
    private val authUtil: FirebaseAuthUtil
) : CoreViewModel() {
    fun isLoggedIn(): Boolean {
        return authUtil.isLoggedIn()
    }
}
