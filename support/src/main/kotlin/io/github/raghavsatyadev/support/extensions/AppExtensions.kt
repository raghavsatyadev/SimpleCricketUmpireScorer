package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.content.Intent
import java.security.MessageDigest
import java.util.UUID

object AppExtensions {

  inline val <T : Any> T.kotlinFileName: String
    get() = javaClass.simpleName + ".kt"

  fun generateRandomNonce(): String {
    val rawNonce = UUID.randomUUID().toString()
    val bytes = rawNonce.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

    return hashedNonce
  }

  fun Context.restartApp() {
    val packageManager = packageManager
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    intent?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) }
    startActivity(intent)
  }
}
