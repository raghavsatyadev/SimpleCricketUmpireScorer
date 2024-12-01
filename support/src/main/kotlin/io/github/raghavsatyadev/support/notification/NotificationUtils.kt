package io.github.raghavsatyadev.support.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationManagerCompat
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.ResourceExtensions.getConColor
import io.github.raghavsatyadev.support.extensions.ResourceExtensions.getConDrawable
import java.net.HttpURLConnection
import java.net.URL
import java.util.Random

@Suppress("unused")
object NotificationUtils {
    private const val MINIMUM = 1

    fun Context.sendNotification(
        notificationId: Int,
        title: String?,
        message: String?,
        imageURL: String?,
        channelId: String?,
        intent: Intent?,
        pendingIntentFlag: Int,
    ): Int? {
        var channelIdChanged = channelId

        var notificationIdChanged = notificationId

        if (notificationIdChanged <= 1) {
            notificationIdChanged = randomNotificationID
        }

        val notificationManager = getNotificationManager()

        val appName: String = getString(R.string.app_name)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            channelIdChanged =
                getNotificationChannelID(notificationManager, channelIdChanged, defaultSoundUri)
        }

        val builder: NotificationCompat.Builder =
            getNotificationBuilder(
                if (!TextUtils.isEmpty(title)) title else appName,
                message,
                imageURL,
                defaultSoundUri,
                channelIdChanged,
                PendingIntent.getActivity(this, notificationIdChanged, intent, pendingIntentFlag)
            )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }
        notificationManager.notify(notificationIdChanged, builder.build())
        return notificationIdChanged
    }

    private fun Context?.getNotificationManager(): NotificationManagerCompat {
        return NotificationManagerCompat.from(this!!)
    }

    @RequiresApi(api = VERSION_CODES.O)
    fun Context.getNotificationChannelID(
        notificationManager: NotificationManagerCompat,
        channelId: String?,
        defaultSoundUri: Uri,
    ): String {
        val id =
            if (TextUtils.isEmpty(channelId)) getString(R.string.general_notification_channel_id) else channelId!!
        val notificationChannel = createNotificationChannel(id, defaultSoundUri)
        notificationManager.createNotificationChannel(notificationChannel)
        return id
    }

    @RequiresApi(api = VERSION_CODES.O)
    fun Context.createNotificationChannel(
        id: String?,
        defaultSoundUri: Uri,
    ): NotificationChannelCompat {
        // The user-visible name of the channel.
        val channelName: String = getString(R.string.general_notification_channel_name)
        val channelDescription: String =
            getString(R.string.general_notification_channel_description)
        val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
        return getNotificationChannelBuilder(
            id,
            channelName,
            channelDescription,
            importance,
            defaultSoundUri
        ).build()
    }

    private fun getNotificationChannelBuilder(
        id: String?,
        channelName: String?,
        channelDescription: String,
        @Suppress("SameParameterValue")
        importance: Int,
        defaultSoundUri: Uri,
    ): NotificationChannelCompat.Builder {
        return NotificationChannelCompat.Builder(id!!, importance).apply {
            setName(channelName)
            setSound(defaultSoundUri, null)
            setDescription(channelDescription)
            setShowBadge(true)
            setVibrationEnabled(true)
            setLightsEnabled(true)
        }
    }

    private fun Context.getNotificationBuilder(
        title: String?,
        message: String?,
        imageURL: String?,
        defaultSoundUri: Uri,
        channelId: String?,
        pendingIntent: PendingIntent?,
    ): NotificationCompat.Builder {
        var channelIdChanged = channelId

        if (!TextUtils.isEmpty(channelIdChanged)) channelIdChanged =
            getString(R.string.general_notification_channel_id)

        val icLauncher = getBitmapFromDrawable(getConDrawable(bigIcon)!!)

        val notificationColor: Int = getConColor(R.color.notification_color)

        val builder = NotificationCompat.Builder(this, channelIdChanged!!).apply {
            setAutoCancel(true)
            setSmallIcon(smallIcon)
            setLargeIcon(icLauncher)
            setSound(defaultSoundUri)
            color = notificationColor
            setContentTitle(title)
            setContentText(message)
            setTicker(title)
            setColorized(true)
            setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            if (pendingIntent != null) setContentIntent(pendingIntent)
        }

        return setNotificationStyle(builder, icLauncher, imageURL, title, message)
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    private val bigIcon: Int
        get() = R.mipmap.ic_launcher

    private val smallIcon: Int
        get() = R.drawable.ic_small_icon

    private fun setNotificationStyle(
        builder: NotificationCompat.Builder,
        icLauncher: Bitmap, imageURL: String?,
        title: String?,
        message: String?,
    ): NotificationCompat.Builder {
        builder.apply {
            if (!TextUtils.isEmpty(imageURL)) {
                setStyle(
                    BigPictureStyle()
                        .bigLargeIcon(icLauncher)
                        .setSummaryText(message)
                        .setBigContentTitle(title)
                        .bigPicture(getBitmapFromUrl(imageURL))
                )
            } else {
                setStyle(BigTextStyle().bigText(message))
            }
        }

        return builder
    }

    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            AppLog.loge(false, kotlinFileName, "getBitmapFromUrl", e, Exception())
            null
        }
    }

    val randomNotificationID: Int
        get() = Random().nextInt(Int.MAX_VALUE - MINIMUM) + MINIMUM
}