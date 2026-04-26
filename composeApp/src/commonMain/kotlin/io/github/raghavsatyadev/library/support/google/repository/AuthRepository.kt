package io.github.raghavsatyadev.library.support.google.repository

import io.github.raghavsatyadev.library.support.models.User
import io.github.raghavsatyadev.library.support.models.essential.CustomError

interface AuthRepository {
  val currentUserId: String?

  suspend fun signInWithGoogle(idToken: String): Pair<User?, CustomError?>

  suspend fun validateLoginToken(user: User): LoginTokenStatus

  suspend fun updateUserTokens()

  fun isLoggedIn(): Boolean

  suspend fun signOut()
}

sealed class LoginTokenStatus {
  object Success : LoginTokenStatus()

  data class Error(val exception: Exception) : LoginTokenStatus()

  object RemoteTokenMismatch : LoginTokenStatus()
}
