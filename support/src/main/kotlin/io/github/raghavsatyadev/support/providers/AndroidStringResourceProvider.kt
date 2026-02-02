package io.github.raghavsatyadev.support.providers

import android.content.Context
import androidx.annotation.StringRes

class AndroidStringResourceProvider(private val context: Context) : StringResourceProvider {
  override fun getString(@StringRes stringResId: Int): String {
    return context.getString(stringResId)
  }

  override fun getString(@StringRes stringResId: Int, vararg formatArgs: Any): String {
    return context.getString(stringResId, *formatArgs)
  }
}
