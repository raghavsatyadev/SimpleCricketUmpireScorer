package io.github.raghavsatyadev.support.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

@Suppress("unused")
object ViewExtensions {
  fun View.gone() {
    if (visibility == View.GONE) return
    visibility = View.GONE
  }

  fun View.visible() {
    if (visibility == View.VISIBLE) return
    visibility = View.VISIBLE
  }

  fun View.invisible() {
    if (visibility == View.INVISIBLE) return
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
