package io.github.raghavsatyadev.support.google

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.firestore.ktx.persistentCacheSettings
import com.google.firebase.ktx.Firebase
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.Constants.FieldKeys.SERVER_UPDATE_DATE_TIME
import io.github.raghavsatyadev.support.Constants.FirebaseConstants
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil
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
            return instance ?: throw IllegalStateException(
                "Initialize in Application class using create()"
            )
        }
    }

    private val db by lazy {
        Firebase
            .firestore(firebaseApp)
            .apply {
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
        val task = db
            .collection(FirebaseConstants.Collections.USER)
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
        val task = db
            .collection(FirebaseConstants.Collections.USER)
            .document(id)
            .get()
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

    // region MatchRecord
    suspend fun setMatchRecord(matchRecord: MatchRecord): MatchRecord {
        val document = db
            .collection(FirebaseConstants.Collections.MATCH_RECORD)
            .document()
        matchRecord.matchRecordId = document.id
        val task = db.runTransaction {
            it.set(
                document,
                matchRecord
            )
            it.update(
                document,
                SERVER_UPDATE_DATE_TIME,
                FieldValue.serverTimestamp()
            )
        }

        with(task) {
            await()
            if (!isSuccessful) {
                throw exception ?: Exception("Unknown Exception")
            } else {
                val readTask = document.get()
                readTask.await()
                if (readTask.isSuccessful) {
                    try {
                        val record: MatchRecord =  readTask.result.toDataObject<MatchRecord>()
                        MatchRecordDataUtil
                            .getInstance()
                            .insertIgnore(record)
                        return record
                    } catch (e: Exception) {
                        AppLog.loge(
                            false,
                            kotlinFileName,
                            "setMatchRecord",
                            e,
                            Exception()
                        )
                        throw e
                    }
                }
                throw Exception("Unknown Exception")
            }
        }
    }
    // endregion

    private inline fun <reified T> DocumentSnapshot.toDataObject(): T = data
        .toJsonString()
        .toKotlinObject()
}