package io.github.raghavsatyadev.support.google

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.firestore.ktx.persistentCacheSettings
import com.google.firebase.ktx.Firebase
import io.github.raghavsatyadev.support.Constants.FirebaseConstants
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.User
import kotlinx.coroutines.tasks.await

class FireStoreUtil private constructor(private val firebaseApp: FirebaseApp) {
    companion object {
        @Volatile
        private var instance: FireStoreUtil? = null

        @Synchronized
        fun create(firebaseApp: FirebaseApp) {
            FireStoreUtil(firebaseApp).also { instance = it }
        }

        @Synchronized
        fun getInstance(): FireStoreUtil {
            return instance
                ?: throw IllegalStateException("Initialize in Application class using create()")
        }
    }

    private val db by lazy {
        Firebase.firestore(firebaseApp).apply {
            val settings = firestoreSettings {
                setLocalCacheSettings(memoryCacheSettings {})
                setLocalCacheSettings(persistentCacheSettings {})
            }
            firestoreSettings = settings
            persistentCacheIndexManager?.apply {
                enableIndexAutoCreation()
            } ?: println("indexManager is null")
        }
    }

    // region User
    suspend fun setUser(user: User): User {
        val task =
            db.collection(FirebaseConstants.Collections.USER)
                .document(user.userID)
                .set(user)

        with(task) {
            await()
            if (isSuccessful) {
                return user
            } else {
                throw exception ?: Exception("Unknown Exception")
            }
        }
    }

    suspend fun readUser(id: String): User {
        val task = db.collection(FirebaseConstants.Collections.USER).document(id).get()
        with(task) {
            await()
            if (isSuccessful) {
                return result.toDataObject()
            } else {
                throw exception ?: Exception("Unknown Exception")
            }
        }
    }
    // endregion


    private inline fun <reified T> DocumentSnapshot.toDataObject(): T =
        data.toJsonString().toKotlinObject()
}