package io.github.raghavsatyadev.library.google.repository

import io.github.raghavsatyadev.library.models.User
import io.github.raghavsatyadev.library.models.essential.CustomError

interface AuthRepository {
  val currentUserId: String?

  suspend fun signInWithGoogle(idToken: String): Pair<User?, CustomError?>

  suspend fun validateLoginToken(user: User): LoginTokenStatus

  suspend fun updateUserTokens()

  suspend fun signOut()
}

sealed class LoginTokenStatus {
  object Success : LoginTokenStatus()

  data class Error(val exception: Exception) : LoginTokenStatus()

  object RemoteTokenMismatch : LoginTokenStatus()
}
