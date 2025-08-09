package io.github.raghavsatyadev.support.extensions.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

object SerializationExtensions {
  @OptIn(ExperimentalSerializationApi::class)
  val kotlinJsonSerializer = Json {
    ignoreUnknownKeys = true
    serializersModule = SerializersModule {
      contextual(ExceptionSerializer)
      contextual(DynamicLookupSerializer)
      contextual(TimeStampSerializer)
      contextual(DateSerializer)
      contextual(InstantSerializer)
      contextual(HashMapSerializer)
      contextual(ArrayListSerializer)
    }
    prettyPrint = true
  }

  inline fun <reified T> String.toKotlinObject(): T {
    return kotlinJsonSerializer.decodeFromString(this)
  }

  inline fun <reified T> T.toJsonString(): String {
    return kotlinJsonSerializer.encodeToString(this)
  }
}
