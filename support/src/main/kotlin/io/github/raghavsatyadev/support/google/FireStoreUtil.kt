package io.github.raghavsatyadev.support.google

import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.memoryCacheSettings
import com.google.firebase.firestore.ktx.persistentCacheSettings
import com.google.firebase.ktx.Firebase
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.Constants.FieldKeys
import io.github.raghavsatyadev.support.Constants.FieldKeys.SERVER_UPDATE_DATE_TIME
import io.github.raghavsatyadev.support.Constants.FirebaseConstants
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.AppExtensions
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.AppExtensions.restartApp
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.models.User
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecord
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordDataUtil
import io.github.raghavsatyadev.support.models.db.match_record.MatchRecordExtensions.toMap
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FireStoreUtil private constructor(private val firebaseApp: FirebaseApp) {
    companion object {
        @Volatile
        private var instance: FireStoreUtil? = null

        @Synchronized
        fun create(firebaseApp: FirebaseApp) {
            FireStoreUtil(firebaseApp).also { instance = it }
            CoreApp.instance.launch {
                getInstance().initialize(true)
            }
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

    suspend fun initialize(checkUserToken: Boolean = false) {
        try {
            val currentUserId = FirebaseAuthUtil.getInstance().currentUserId
            if (currentUserId != null) {
                if (checkUserToken) {
                    val user = getUser(currentUserId)
                    val userToken = AppPrefsUtil
                        .getUserToken()
                        .firstOrNull()
                    if (user.loginToken != userToken) {
                        AppExtensions.signOut()
                        CoreApp.instance.restartApp()
                        return
                    }
                }
                val matchRecordTask = db
                    .collection(FirebaseConstants.Collections.MATCH_RECORD)
                    .where(
                        Filter.or(
                            Filter.equalTo(
                                FieldKeys.MATCH_ADMIN_ID,
                                currentUserId
                            ),
                            Filter.arrayContains(
                                FieldKeys.MATCH_SHARED_USER_IDS,
                                currentUserId
                            )
                        )
                    )
                    .get()
                with(matchRecordTask) {
                    await()
                    if (isSuccessful) {
                        val records = result.toDataObjects<MatchRecord>()
                        MatchRecordDataUtil
                            .getInstance()
                            .upsert(records)
                    }
                }
            }
        } catch (e: Exception) {
            AppLog.loge(
                false,
                kotlinFileName,
                "initialize",
                e,
                Exception()
            )
        }
    }

    // region User
    suspend fun setUser(user: User): User {
        val generateRandomNonce = AppExtensions.generateRandomNonce()
        user.loginToken = generateRandomNonce
        val task = db
            .collection(FirebaseConstants.Collections.USER)
            .document(user.userID)
            .set(user)

        with(task) {
            await()
            if (isSuccessful) {
                AppPrefsUtil.saveUserToken(generateRandomNonce)
                return user
            } else {
                throw exception ?: Exception("Unknown Exception")
            }
        }
    }

    @Throws(Exception::class)
    suspend fun updateUserLoginTokens(): Boolean {
        val currentUserId = FirebaseAuthUtil.getInstance().currentUserId
        currentUserId?.let {
            val generateRandomNonce = AppExtensions.generateRandomNonce()
            val task = db
                .collection(FirebaseConstants.Collections.USER)
                .document(it)
                .update(
                    FieldKeys.LOGIN_TOKEN,
                    generateRandomNonce,
                )
            task.await()
            val successful = task.isSuccessful
            if (successful) AppPrefsUtil.saveUserToken(generateRandomNonce)
            return successful
        }
        return false
    }

    @Throws(Exception::class)
    suspend fun signOutUser(): Boolean {
        val currentUserId = FirebaseAuthUtil.getInstance().currentUserId
        currentUserId?.let {
            val task = db
                .collection(FirebaseConstants.Collections.USER)
                .document(it)
                .update(
                    FieldKeys.LOGIN_TOKEN,
                    null,
                )
            task.await()
            return task.isSuccessful
        }
        return false
    }

    suspend fun getUser(id: String): User {
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
    suspend fun createMatchRecord(matchRecord: MatchRecord): MatchRecord {
        val document = db
            .collection(FirebaseConstants.Collections.MATCH_RECORD)
            .document()
        matchRecord.matchRecordId = document.id
        val task = db.runTransaction {
            it[document] = matchRecord
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
                        val record: MatchRecord = readTask.result.toDataObject<MatchRecord>()
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

    suspend fun updateMatchRecords(matchRecords: List<MatchRecord>): Boolean {
        val task = db.runTransaction {
            matchRecords.forEach { record ->
                val document = db
                    .collection(FirebaseConstants.Collections.MATCH_RECORD)
                    .document(record.matchRecordId)
                it[document] = record
                it.update(
                    document,
                    record.toMap()
                )
            }
        }
        with(task) {
            await()
            if (!isSuccessful) {
                throw exception ?: Exception("Unknown Exception")
            }
            return true
        }
    }

    suspend fun updateMatchRecord(matchRecord: MatchRecord): Boolean {
        val document = db
            .collection(FirebaseConstants.Collections.MATCH_RECORD)
            .document()
        matchRecord.matchRecordId = document.id
        val task = document.update(
            SERVER_UPDATE_DATE_TIME,
            FieldValue.serverTimestamp()
        )

        with(task) {
            await()
            if (!isSuccessful) {
                throw exception ?: Exception("Unknown Exception")
            } else {
                val readTask = document.get()
                readTask.await()
                if (readTask.isSuccessful) {
                    try {
                        val record: MatchRecord = readTask.result.toDataObject<MatchRecord>()
                        MatchRecordDataUtil
                            .getInstance()
                            .updateServerTime(
                                record.matchRecordId,
                                record.serverUpdateDateTime
                            )
                        return true
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

    suspend fun deleteMatchRecord(matchRecordId: String): Boolean {
        val task = db
            .collection(FirebaseConstants.Collections.MATCH_RECORD)
            .document(matchRecordId)
            .delete()
        with(task) {
            await()
            return isSuccessful
        }
    }
// endregion

    private inline fun <reified T> DocumentSnapshot.toDataObject(): T = data
        .toJsonString()
        .toKotlinObject()

    private inline fun <reified T> QuerySnapshot.toDataObjects(): List<T> = documents.map {
        it.toDataObject()
    }
}