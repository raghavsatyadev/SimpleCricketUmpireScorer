package io.github.raghavsatyadev.support.google

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.essential.CustomError
import io.github.raghavsatyadev.support.models.essential.ErrorCode
import kotlinx.coroutines.tasks.await

/**
 * Utility class to handle Firebase Authentication operations with Google
 * Sign-In.
 */
class FirebaseAuthUtil private constructor(private val firebaseApp: FirebaseApp) {
    companion object {
        @Volatile
        private var instance: FirebaseAuthUtil? = null

        @Synchronized
        fun create(firebaseApp: FirebaseApp) {
            FirebaseAuthUtil(firebaseApp).also { instance = it }
        }

        @Synchronized
        fun getInstance(): FirebaseAuthUtil {
            return instance ?: throw IllegalStateException(
                "Initialize in Application class using create()"
            )
        }
    }

    private val auth: FirebaseAuth by lazy { Firebase.auth(firebaseApp) }

    /**
     * Retrieves the currently signed-in user, if any.
     *
     * @return The currently signed-in [User], or null if no user is signed in.
     */
    val currentUser: User?
        get() = auth.currentUser?.let {
            User(
                name = it.displayName ?: "",
                email = it.email ?: "",
                userID = it.uid
            )
        }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    /** Reloads the currently signed-in user's data. */
    fun reloadAuthCurrentUser() {
        auth.currentUser?.reload()
    }

    /**
     * Retrieves the unique identifier of the currently signed-in user.
     *
     * @return The user ID, or null if no user is signed in.
     */
    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Signs in a user using the provided Google ID token.
     *
     * @param idToken The Google ID token obtained from the sign-in process.
     * @return A [Pair] containing the signed-in [User] and a [CustomError] if
     *    any occurred.
     */
    suspend fun signInWithGoogle(idToken: String): Pair<User?, CustomError?> {
        val credential = GoogleAuthProvider.getCredential(
            idToken,
            null
        )
        return try {
            val authResult = auth
                .signInWithCredential(credential)
                .await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    userID = firebaseUser.uid
                )
                try {
                    user to null
                } catch (e: Exception) {
                    AppLog.loge(
                        false,
                        kotlinFileName,
                        "signInWithGoogle",
                        e,
                        Exception()
                    )
                    null to CustomError(
                        ErrorCode.AUTH_FAILED,
                        e
                    )
                }
            } else {
                val e = Exception("Firebase user is null")
                AppLog.loge(
                    false,
                    kotlinFileName,
                    "signInWithGoogle",
                    e,
                    Exception()
                )
                null to CustomError(
                    ErrorCode.AUTH_FAILED,
                    e
                )
            }
        } catch (e: Exception) {
            AppLog.loge(
                false,
                kotlinFileName,
                "signInWithGoogle",
                e,
                Exception()
            )
            null to CustomError(
                ErrorCode.AUTH_FAILED,
                e
            )
        }
    }

    /** Signs out the currently signed-in user. */
    fun signOut() {
        auth.signOut()
    }
}
