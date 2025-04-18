package io.github.raghavsatyadev.support.preferences

import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.deleteAllPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.getPrefs
import io.github.raghavsatyadev.support.preferences.AppPrefsExtensions.savePref
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.NOTIFICATION_ENABLED
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOKEN
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.FCM.TOPICS
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.Keys.USER_TOKEN
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AppPrefsUtil {
    suspend fun clearAppPreferences() {
        CoreApp.instance.deleteAllPrefs()
    }

    suspend fun saveFCMToken(token: String) {
        CoreApp.instance.savePref(TOKEN, token)
    }

    fun getFCMToken(): Flow<String?> {
        return CoreApp.instance.getPrefs(TOKEN)
    }

    suspend fun setFCMTopics(fcmTopics: ArrayList<String>) {
        CoreApp.instance.savePref(TOPICS, fcmTopics.toJsonString())
    }

    fun getFCMTopics(): Flow<ArrayList<String>> {
        return CoreApp.instance.getPrefs(TOPICS, "[]").map { it.toKotlinObject() }
    }

    suspend fun setNotificationEnableStatus(isNotificationEnabled: Boolean) {
        CoreApp.instance.savePref(NOTIFICATION_ENABLED, isNotificationEnabled)
    }

    fun isNotificationEnabled(): Flow<Boolean> {
        return CoreApp.instance.getPrefs(NOTIFICATION_ENABLED, true)
    }

    fun getUserToken(): Flow<String?> {
        return CoreApp.instance.getPrefs(USER_TOKEN)
    }

    suspend fun saveUserToken(token: String) {
        CoreApp.instance.savePref(USER_TOKEN, token)
    }

    object Keys {
        const val USER_TOKEN = "user_token"
    }

    object FCM {
        const val TOKEN = "token"
        const val TOPICS = "topics"
        const val NOTIFICATION_ENABLED = "notification_enabled"
    }
}
