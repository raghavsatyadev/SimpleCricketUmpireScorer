package io.github.raghavsatyadev.support.google.repository

import io.github.raghavsatyadev.support.AppHelpers
import io.github.raghavsatyadev.support.database.RoomDBUtil
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.essential.CustomError

class AuthRepositoryImpl(
  private val authUtil: FirebaseAuthUtil,
  private val fireStoreRepository: FireStoreRepository,
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
        fireStoreRepository.getUser(user.userID)
      } catch (_: Exception) {
        null
      }

    return when {
      remoteUser == null -> {
        try {
          fireStoreRepository.setUser(user)
          fireStoreRepository.initialize()
          LoginTokenStatus.Success
        } catch (e: Exception) {
          LoginTokenStatus.Error(e)
        }
      }
      remoteUser.loginToken.isNullOrEmpty() -> {
        try {
          fireStoreRepository.updateUserLoginTokens()
          fireStoreRepository.initialize()
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
    fireStoreRepository.updateUserLoginTokens()
    fireStoreRepository.initialize()
  }

  override suspend fun signOut() {
    AppHelpers.signOut(fireStoreRepository, authUtil, roomDBUtil)
  }
}
