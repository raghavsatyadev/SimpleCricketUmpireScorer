package io.github.raghavsatyadev.support

import com.google.android.libraries.identity.googleid.GetGoogleIdOption

class GoogleSignInUtil {
    fun create() {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()
    }
}