package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.content.res.Configuration
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatTextView

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
        @ColorInt
        color: Int? = null,
        applyColor: Boolean = false,
        partStrings: Array<String>,
        listener: ((String) -> Unit),
    ) {
        movementMethod = LinkMovementMethod.getInstance()
        text = buildClickableForegroundSpan(
            fullString,
            color,
            applyColor,
            partStrings,
            listener
        )
    }

    private fun buildClickableForegroundSpan(
        fullString: String,
        @ColorInt
        color: Int? = null,
        applyColor: Boolean = false,
        partStrings: Array<String>,
        listener: ((String) -> Unit),
    ): SpannableStringBuilder {
        val ssb = SpannableStringBuilder(fullString)

        partStrings.forEach { value ->
            val indexOf = fullString.indexOf(value)
            ssb.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    widget.cancelPendingInputEvents()
                    listener(value)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isFakeBoldText = true
                    ds.isUnderlineText = false
                }
            }, indexOf, indexOf + value.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            if (applyColor) {
                color?.let {
                    ssb.setSpan(
                        ForegroundColorSpan(color),
                        indexOf,
                        indexOf + value.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        return ssb
    }

    fun Context.isNightModeEnabled(): Boolean {
        val currentNightMode =
            this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}