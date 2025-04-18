package io.github.raghavsatyadev.support.compose.google

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthUtil @Inject constructor(private val firebaseApp: FirebaseApp) {
  private val auth: FirebaseAuth by lazy { Firebase.auth(firebaseApp) }

  /** Wraps FirebaseUser into your app’s User model */
  val currentUser: User?
    get() =
      auth.currentUser?.let {
        User(name = it.displayName.orEmpty(), email = it.email.orEmpty(), userID = it.uid)
      }

  /**
   * Retrieves the unique identifier of the currently signed-in user.
   *
   * @return The user ID, or null if no user is signed in.
   */
  val currentUserId: String?
    get() = auth.currentUser?.uid

  fun isLoggedIn(): Boolean {
    val user = auth.currentUser
    return user != null
  }

  /** Google‑sign‑in flow */
  suspend fun signInWithGoogle(idToken: String): Pair<User?, CustomError?> {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    return try {
      val result = auth.signInWithCredential(credential).await()
      result.user?.let { User(it.displayName.orEmpty(), it.email.orEmpty(), it.uid) to null }
        ?: run {
          val e = Exception("Firebase user was null")
          AppLog.loge(false, "HiltFirebaseAuthUtil", "signInWithGoogle", e, Exception())
          null to CustomError(ErrorCode.AUTH_FAILED, e)
        }
    } catch (e: Exception) {
      AppLog.loge(false, "HiltFirebaseAuthUtil", "signInWithGoogle", e, Exception())
      null to CustomError(ErrorCode.AUTH_FAILED, e)
    }
  }

  /** Sign‑out */
  fun signOut() = auth.signOut()
}
