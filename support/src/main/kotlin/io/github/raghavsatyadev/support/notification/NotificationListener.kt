package io.github.raghavsatyadev.support.notification

import android.text.TextUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.getFCMTopics
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.isNotificationEnabled
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.saveFCMToken
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil.setFCMTopics
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

@Suppress("UNUSED_PARAMETER")
class NotificationListener : FirebaseMessagingService(), CoroutineScope {
    private lateinit var job: Job

    private val handler = CoroutineExceptionHandler { _, exception ->
        AppLog.loge(false, kotlinFileName, "handler", exception, Exception())
    }

    override fun onCreate() {
        super.onCreate()
        job = Job()
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    override val coroutineContext: CoroutineContext
        get() {
            var context = Dispatchers.Main + job
            if (!BuildConfig.DEBUG) context += handler
            return context
        }

    override fun onMessageReceived(packet: RemoteMessage) {
        val from = packet.from
        val message = getMessageFromData(packet)
        launch {
            handlePacketMessage(from, message)
        }
    }

    private suspend fun handlePacketMessage(from: String?, message: String?) {
        if (from != null) {
            if (from.startsWith("/topics/")) {
                val topic = from.replace("/topics/", "")
                try {
                    if (getFCMTopics().first().contains(topic)) {
                        traverseMessage(message)
                    }
                } catch (e: NullPointerException) {
                    AppLog.loge(
                        false,
                        kotlinFileName,
                        "onMessageReceived",
                        e,
                        Exception()
                    )
                }
            } else {
                message?.let { traverseMessage(it) }
            }
        }
    }

    private fun getMessageFromData(
        packet: RemoteMessage,
    ): String? {
        var message: String? = null
        if (packet.data.isNotEmpty()) {
            try {
                message = getMessageFromPacket(packet)
            } catch (e: JSONException) {
                AppLog.loge(false, kotlinFileName, "handleDataMessage", e, Exception())
            }
        } else if (packet.notification != null && !TextUtils.isEmpty(packet.notification?.body)) {
            message = packet.notification!!.body
        } else {
            message = getString(R.string.default_notification_message)
        }
        return message
    }

    private fun getMessageFromPacket(packet: RemoteMessage): String {
        val data = packet.data
        var message = data[Constants.NotificationKeys.MAIN_KEY].toString()
        if (TextUtils.isEmpty(message) || message == "null") {
            val keys: Iterator<String> = data.keys.iterator()
            val jsonObject = JSONObject()
            if (keys.hasNext()) {
                while (keys.hasNext()) {
                    val key = keys.next()
                    jsonObject.put(key, data[key])
                }
                message = jsonObject.toString()
            }
        }
        return message
    }

    @Suppress("ControlFlowWithEmptyBody")
    private suspend fun traverseMessage(notificationData: String?) {
        if (isNotificationEnabled().first()) {
        }
    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        launch {
            saveTokenProcess(token)
        }
    }

    companion object {
        private val defaultDispatcher = Dispatchers.Default

        suspend fun saveTokenProcess(token: String) {
            if (BuildConfig.DEBUG) {
                AppLog.loge(true, kotlinFileName, "saveTokenProcess", token, Exception())
            }

            saveFCMToken(token)
            subscribeTopics(arrayListOf())
        }

        suspend fun recreateToken() {
            withContext(defaultDispatcher) {
                Tasks.await(FirebaseMessaging.getInstance().deleteToken())
                Tasks.await(FirebaseMessaging.getInstance().token)
            }
        }

        private suspend fun subscribeTopics(topics: ArrayList<String>) {
            unSubscribeTopics()
            topics.add(CoreApp.instance.getString(R.string.app_name))
            val pubSub = FirebaseMessaging.getInstance()
            val modifiedTopics = ArrayList<String>()
            for (i in 0 until topics.size) {
                try {
                    val topic = makeTopic(topics[i])
                    modifiedTopics.add(topic)
                    pubSub.subscribeToTopic(topic)
                } catch (e: JSONException) {
                    AppLog.loge(false, kotlinFileName, "subscribeTopics", e, Exception())
                }
            }
            setFCMTopics(modifiedTopics)
        }

        suspend fun unSubscribeTopics() {
            val fcmTopics = (getFCMTopics().first())
            fcmTopics.let {
                val pubSub = FirebaseMessaging.getInstance()
                for (i in 0 until it.size) {
                    pubSub.unsubscribeFromTopic(it[i])
                }
                setFCMTopics(arrayListOf())
            }
        }

        private fun makeTopic(string: String): String {
            return string.replace("(\\W|^_)*".toRegex(), "")
        }
    }
}