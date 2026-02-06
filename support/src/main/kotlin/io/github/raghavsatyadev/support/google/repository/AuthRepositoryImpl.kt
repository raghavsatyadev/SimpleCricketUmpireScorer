package io.github.raghavsatyadev.support.google.repository

import io.github.raghavsatyadev.support.AppHelpers
import io.github.raghavsatyadev.support.database.RoomDBUtil
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.essential.CustomError

class AuthRepositoryImpl(
  private val authUtil: FirebaseAuthUtil,
  private val fireStoreUtil: FireStoreUtil,
  private val roomDBUtil: RoomDBUtil,
) : AuthRepository {

  override val currentUserId: String?
    get() = authUtil.currentUserId

  override suspend fun signInWithGoogle(idToken: String): Pair<User?, CustomError?> {
    return authUtil.signInWithGoogle(idToken)
  }

  override suspend fun validateLoginToken(user: User): LoginTokenStatus {
    val remoteUser =
      try {
        fireStoreUtil.getUser(user.userID)
      } catch (_: Exception) {
        null
      }

    return when {
      remoteUser == null -> {
        try {
          fireStoreUtil.setUser(user)
          fireStoreUtil.initialize()
          LoginTokenStatus.Success
        } catch (e: Exception) {
          LoginTokenStatus.Error(e)
        }
      }
      remoteUser.loginToken.isNullOrEmpty() -> {
        try {
          fireStoreUtil.updateUserLoginTokens()
          fireStoreUtil.initialize()
          LoginTokenStatus.Success
        } catch (e: Exception) {
          LoginTokenStatus.Error(e)
        }
      }
      else -> {
        LoginTokenStatus.RemoteTokenMismatch
      }
    }
  }

  override suspend fun updateUserTokens() {
    fireStoreUtil.updateUserLoginTokens()
    fireStoreUtil.initialize()
  }

  override suspend fun signOut() {
    AppHelpers.signOut(fireStoreUtil, authUtil, roomDBUtil)
  }
}
