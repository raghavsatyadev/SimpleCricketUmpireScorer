package io.github.raghavsatyadev.support.providers

import androidx.annotation.StringRes

interface StringResourceProvider {
  fun getString(@StringRes stringResId: Int): String

  fun getString(@StringRes stringResId: Int, vararg formatArgs: Any): String
}
