package io.github.raghavsatyadev.library.extensions

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun <T : NavKey> NavBackStack<T>.replaceAll(destination: T) {
  clear()
  add(destination)
}

fun <T : NavKey> NavBackStack<T>.replaceAll(destinations: List<T>) {
  clear()
  addAll(destinations)
}

fun <T : NavKey> NavBackStack<T>.removeTill(destination: T, inclusive: Boolean = false) {
  val index = indexOf(destination)
  if (index != -1) {
    for (i in size - 1 downTo index + 1) {
      removeAt(i)
    }
    if (inclusive) {
      removeAt(index)
    }
  }
}
