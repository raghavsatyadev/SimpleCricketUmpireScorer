package io.github.raghavsatyadev.library.support.google.repository

interface AuthRepository {
  val currentUserId: String?

  suspend fun signInWithGoogle(idToken: String): Pair<io.github.raghavsatyadev.library.support.models.User?, io.github.raghavsatyadev.library.support.models.essential.CustomError?>

  suspend fun validateLoginToken(user: io.github.raghavsatyadev.library.support.models.User): LoginTokenStatus

  suspend fun updateUserTokens()

  suspend fun signOut()
}

sealed class LoginTokenStatus {
  object Success : LoginTokenStatus()

  data class Error(val exception: Exception) : LoginTokenStatus()

  object RemoteTokenMismatch : LoginTokenStatus()
}
