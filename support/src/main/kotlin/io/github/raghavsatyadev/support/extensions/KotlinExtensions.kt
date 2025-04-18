package io.github.raghavsatyadev.support.extensions

import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

object KotlinExtensions {
  fun <T> List<T>.forEachParallel(action: Consumer<T>) {
    parallelStream().forEach(action)
  }

  fun <T> Stream<T>.toArrayList(): ArrayList<T> {
    return collect(Collectors.toCollection { ArrayList() })
  }
}
