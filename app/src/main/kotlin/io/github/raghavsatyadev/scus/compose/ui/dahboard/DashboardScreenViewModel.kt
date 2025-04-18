package io.github.raghavsatyadev.scus.compose.ui.dahboard

import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.raghavsatyadev.support.compose.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.core.CoreViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardScreenViewModel
@Inject
constructor(
    private val authUtil: FirebaseAuthUtil
) : CoreViewModel() {
    fun isLoggedIn(): Boolean {
        return authUtil.isLoggedIn()
    }
}
