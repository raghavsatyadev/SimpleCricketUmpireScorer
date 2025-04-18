@file:Suppress("unused")

package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.Locale

@Suppress("MemberVisibilityCanBePrivate")
object ResourceExtensions {
  fun getString(format: String, vararg formatArgs: Any?): String {
    return String.format(Locale.getDefault(), format, *formatArgs)
  }

  fun Context.getConDimen(@DimenRes dimenId: Int): Float {
    return resources.getDimension(dimenId)
  }

  fun Fragment.getConDimen(@DimenRes dimenId: Int): Float {
    return requireContext().getConDimen(dimenId)
  }

  fun Context.getConInt(@IntegerRes intRes: Int): Int {
    return resources.getInteger(intRes)
  }

  fun Fragment.getConInt(@IntegerRes intRes: Int): Int {
    return requireContext().getConInt(intRes)
  }

  fun Context.getConDrawable(@DrawableRes drawableId: Int?): Drawable? {
    if (drawableId == null) return null
    return ContextCompat.getDrawable(this, drawableId)
  }

  fun Fragment.getConDrawable(@DrawableRes drawableId: Int?): Drawable? {
    if (drawableId == null) return null
    return requireContext().getConDrawable(drawableId)
  }

  @ColorInt
  fun Context.getConColor(@ColorRes colorId: Int): Int {
    return ContextCompat.getColor(this, colorId)
  }

  @ColorInt
  fun Fragment.getConColor(@ColorRes colorId: Int): Int {
    return requireContext().getConColor(colorId)
  }

  @ColorInt
  fun Context.getAttrColor(@AttrRes attrID: Int): Int {
    return Color.parseColor(getAttrColorString(attrID))
  }

  @ColorInt
  fun Fragment.getAttrColor(@AttrRes attrID: Int): Int {
    return requireContext().getAttrColor(attrID)
  }

  fun Context.getAttrColorStringWithoutHash(@AttrRes attrID: Int): String {
    return getAttributeValue(attrID, "%06X")
  }

  fun Fragment.getAttrColorStringWithoutHash(@AttrRes attrID: Int): String {
    return requireContext().getAttrColorStringWithoutHash(attrID)
  }

  fun Context.getAttributeValue(attrID: Int, format: String): String {
    val outValue = TypedValue()
    theme.resolveAttribute(attrID, outValue, true)
    return format.format(0xFFFFFF and outValue.data)
  }

  fun Fragment.getAttributeValue(attrID: Int, format: String): String {
    return requireContext().getAttributeValue(attrID, format)
  }

  fun Context.getAttrColorString(@AttrRes attrID: Int): String {
    return getAttributeValue(attrID, "#%06X")
  }

  fun Fragment.getAttrColorString(@AttrRes attrID: Int): String {
    return requireContext().getAttrColorString(attrID)
  }

  fun Context.dpToPx(dp: Int): Float {
    return TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      dp.toFloat(),
      resources.displayMetrics,
    )
  }

  fun Fragment.dpToPx(dp: Int): Float {
    return requireContext().dpToPx(dp)
  }

  fun pxToDp(px: Int): Int {
    return (px / Resources.getSystem().displayMetrics.density).toInt()
  }
}
