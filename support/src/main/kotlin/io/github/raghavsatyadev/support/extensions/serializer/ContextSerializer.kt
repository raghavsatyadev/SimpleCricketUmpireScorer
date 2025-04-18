package io.github.raghavsatyadev.support.extensions.serializer

import com.google.firebase.Timestamp
import kotlinx.serialization.ContextualSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import java.time.Instant
import java.util.Date

object ExceptionSerializer : KSerializer<Exception> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Exception", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Exception) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): Exception {
    return Exception(decoder.decodeString())
  }
}

@ExperimentalSerializationApi
object DynamicLookupSerializer : KSerializer<Any> {
  override val descriptor: SerialDescriptor =
    ContextualSerializer(Any::class, null, emptyArray()).descriptor

  @OptIn(InternalSerializationApi::class)
  override fun serialize(encoder: Encoder, value: Any) {
    val actualSerializer =
      encoder.serializersModule.getContextual(value::class) ?: value::class.serializer()
    encoder.encodeSerializableValue(actualSerializer as KSerializer<Any>, value)
  }

  override fun deserialize(decoder: Decoder): Any {
    error("Unsupported")
  }
}

object TimeStampSerializer : KSerializer<Timestamp> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.LONG)

  override fun serialize(encoder: Encoder, value: Timestamp) {
    encoder.encodeLong(value.seconds.times(1000))
  }

  override fun deserialize(decoder: Decoder): Timestamp {
    return Timestamp(Instant.ofEpochSecond(decoder.decodeLong() / 1000))
  }
}

object DateSerializer : KSerializer<Date> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

  override fun serialize(encoder: Encoder, value: Date) {
    encoder.encodeLong(value.time)
  }

  override fun deserialize(decoder: Decoder): Date {
    return Date(decoder.decodeLong())
  }
}

object InstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)

  override fun serialize(encoder: Encoder, value: Instant) {
    encoder.encodeLong(value.toEpochMilli())
  }

  override fun deserialize(decoder: Decoder): Instant {
    val decodeLong = decoder.decodeLong()
    return Instant.ofEpochMilli(decodeLong)
  }
}

@OptIn(ExperimentalSerializationApi::class)
object HashMapSerializer : KSerializer<HashMap<String, Any>> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("new.HashMap", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: HashMap<String, Any>) {
    encoder.encodeSerializableValue(
      MapSerializer(String.serializer(), DynamicLookupSerializer),
      value,
    )
  }

  override fun deserialize(decoder: Decoder): HashMap<String, Any> {
    error("Unsupported")
  }
}

@OptIn(ExperimentalSerializationApi::class)
object ArrayListSerializer : KSerializer<ArrayList<Any>> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("new.ArrayList", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: ArrayList<Any>) {
    encoder.encodeSerializableValue(ListSerializer(DynamicLookupSerializer), value)
  }

  override fun deserialize(decoder: Decoder): ArrayList<Any> {
    decoder.decodeSerializableValue(ListSerializer(DynamicLookupSerializer)).let {
      return ArrayList(it)
    }
  }
}
