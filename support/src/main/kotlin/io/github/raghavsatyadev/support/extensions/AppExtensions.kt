package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.R
import io.github.raghavsatyadev.support.database.RoomDBUtil
import io.github.raghavsatyadev.support.google.FireStoreUtil
import io.github.raghavsatyadev.support.google.FirebaseAuthUtil
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import java.security.MessageDigest
import java.util.UUID

@Suppress("unused")
object AppExtensions {
  /** Clear full cache of application */
  fun Context.clearFullCache() {
    cacheDir.deleteRecursively()
    externalCacheDir?.deleteRecursively()
  }

  inline val <T : Any> T.kotlinFileName: String
    get() = javaClass.simpleName + ".kt"

  fun AppCompatTextView.setClickableForegroundSpan(
    fullString: String,
    @ColorInt color: Int? = null,
    applyColor: Boolean = false,
    partStrings: Array<String>,
    listener: ((String) -> Unit),
  ) {
    movementMethod = LinkMovementMethod.getInstance()
    text = buildClickableForegroundSpan(fullString, color, applyColor, partStrings, listener)
  }

  private fun buildClickableForegroundSpan(
    fullString: String,
    @ColorInt color: Int? = null,
    applyColor: Boolean = false,
    partStrings: Array<String>,
    listener: ((String) -> Unit),
  ): SpannableStringBuilder {
    val ssb = SpannableStringBuilder(fullString)

    partStrings.forEach { value ->
      val indexOf = fullString.indexOf(value)
      ssb.setSpan(
        object : ClickableSpan() {
          override fun onClick(widget: View) {
            widget.cancelPendingInputEvents()
            listener(value)
          }

          override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isFakeBoldText = true
            ds.isUnderlineText = false
          }
        },
        indexOf,
        indexOf + value.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
      )

      if (applyColor) {
        color?.let {
          ssb.setSpan(
            ForegroundColorSpan(color),
            indexOf,
            indexOf + value.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
          )
        }
      }
    }
    return ssb
  }

  fun Context.isNightModeEnabled(): Boolean {
    val currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
  }

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

  fun AppCompatActivity.getAlreadyLoggedInText(): Spanned {
    val title = getString(R.string.warning_already_logged_in_1)
    val bulletPoint1 = getString(R.string.warning_already_logged_in_2)
    val bulletPoint2 = getString(R.string.warning_already_logged_in_3)

    val spannableString = SpannableStringBuilder()

    // Add title
    spannableString.append(title).append("\n\n")

    // Add first bullet point with indentation
    val bullet1 = SpannableString(bulletPoint1)
    bullet1.setSpan(BulletSpan(40), 0, bullet1.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.append(bullet1).append("\n\n")

    // Add second bullet point with indentation
    val bullet2 = SpannableString(bulletPoint2)
    bullet2.setSpan(BulletSpan(40), 0, bullet2.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    spannableString.append(bullet2)

    return spannableString
  }

  suspend fun signOut(doSignOutFromFirestore: Boolean = false) {
    if (doSignOutFromFirestore) {
      FireStoreUtil.getInstance().signOutUser()
    }
    FirebaseAuthUtil.getInstance().signOut()
    RoomDBUtil.deleteAll()
    AppPrefsUtil.clearAppPreferences()
    AppLog.loge(true, kotlinFileName, "signOut", "SignOut", Exception())
  }
}
