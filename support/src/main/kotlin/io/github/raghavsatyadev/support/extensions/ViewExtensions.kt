package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

@Suppress("unused")
object ViewExtensions {
  fun View.gone() {
    if (isGone) return
    visibility = View.GONE
  }

  fun View.visible() {
    if (isVisible) return
    visibility = View.VISIBLE
  }

  fun View.invisible() {
    if (isInvisible) return
    visibility = View.INVISIBLE
  }

  fun View.hideKeyBoard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, 0)
  }

  var EditText.isEditable
    get() = isFocusable && isClickable && isFocusableInTouchMode
    set(canEdit) {
      isFocusable = canEdit
      isClickable = canEdit
      isFocusableInTouchMode = canEdit
    }
}
