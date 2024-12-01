package io.github.raghavsatyadev.support.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.FileExtensions.getUriForFile
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLConnection
import java.net.URLEncoder

@Suppress("unused", "MemberVisibilityCanBePrivate")
object ImplicitIntentExtensions {
    fun Context.openDialer(number: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    fun Fragment.openDialer(number: String) {
        requireContext().openDialer(number)
    }

    fun Context.emailTo(emailID: String?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailID, null))
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }

    fun Context.emailTo(
        emailID: String, subject: String?, mailContent: String?,
    ) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailID))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, mailContent)
        emailIntent.data = Uri.parse("mailto:")
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }

    fun Context.copyToClipboard(label: String? = null, data: String?) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip =
            ClipData.newPlainText(label ?: getString(R.string.app_name), data)
        clipboard.setPrimaryClip(clip)
    }

    fun Fragment.copyToClipboard(label: String? = null, data: String?) {
        requireContext().copyToClipboard(label, data)
    }

    fun Context.openGoogleMaps(keyword: String?) {
        try {
            val mapString =
                "https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(
                    keyword,
                    "utf-8"
                )
            openBrowser(mapString)
        } catch (e: UnsupportedEncodingException) {
            AppLog.loge(false, kotlinFileName, "openGoogleMaps", e, Exception())
        }
    }

    fun getBrowserIntent(url: String?): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    fun Context.openBrowser(url: String?) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    fun Context.openPlayStore(id: String = packageName) {
        val url = getPlayStoreLink(id)
        val intent = getBrowserIntent(url).apply {
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

    fun Context.openPlayServiceUpdate() {
        openPlayStore("com.google.android.gms")
    }

    fun Context.getPlayStoreLink(id: String = packageName): String {
        return "https://play.google.com/store/apps/details?id=$id"
    }

    fun Context.openWhatsapp(phoneNumberWithCountryCode: String) {
        val phoneNumber = phoneNumberWithCountryCode
            .replace("\\+".toRegex(), "")
            .replace(" ".toRegex(), "")
            .replace("\\.".toRegex(), "")
            .replace("\\(".toRegex(), "")
            .replace("\\)".toRegex(), "")
        openBrowser("https://api.whatsapp.com/send?phone=$phoneNumber")
    }

    fun Context.shareApp() {
        shareText(getPlayStoreLink())
    }

    fun Context.shareFile(file: File) {
        ShareCompat.IntentBuilder(this)
            .setStream(file.getUriForFile(this))
            .setType(URLConnection.guessContentTypeFromName(file.name))
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { startActivity(this) }
    }

    fun File.shareFile(context: Context) {
        ShareCompat.IntentBuilder(context)
            .setStream(getUriForFile(context))
            .setType(URLConnection.guessContentTypeFromName(name))
            .createChooserIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .apply { context.startActivity(this) }
    }

    fun Context.shareText(content: String?) {
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }
}