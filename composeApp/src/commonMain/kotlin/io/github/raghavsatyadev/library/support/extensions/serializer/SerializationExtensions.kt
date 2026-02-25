package io.github.raghavsatyadev.library.support.extensions.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object SerializationExtensions {
  @OptIn(ExperimentalSerializationApi::class)
  val kotlinJsonSerializer = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
  }

  inline fun <reified T> String.toKotlinObject(): T {
    return kotlinJsonSerializer.decodeFromString(this)
  }

  inline fun <reified T> T.toJsonString(): String {
    return kotlinJsonSerializer.encodeToString(this)
  }
}
