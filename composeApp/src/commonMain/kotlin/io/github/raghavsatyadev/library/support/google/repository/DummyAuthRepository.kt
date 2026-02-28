package io.github.raghavsatyadev.library.support.google.repository

import io.github.raghavsatyadev.library.support.models.User
import io.github.raghavsatyadev.library.support.models.essential.CustomError
import io.github.raghavsatyadev.library.support.models.essential.ErrorCode

class DummyAuthRepository : AuthRepository {
    override val currentUserId: String? = null

    override suspend fun signInWithGoogle(idToken: String): Pair<User?, CustomError?> {
        return Pair(
                null,
                CustomError(
                        errorCode = ErrorCode.UNKNOWN_ERROR,
                        exception = Exception("Not implemented")
                )
        )
    }

    override suspend fun validateLoginToken(user: User): LoginTokenStatus {
        return LoginTokenStatus.Error(Exception("Not implemented"))
    }

    override suspend fun updateUserTokens() {}

    override fun isLoggedIn(): Boolean = false

    override suspend fun signOut() {}
}
