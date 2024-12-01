package io.github.raghavsatyadev.support.extensions.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

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

val kotlinJsonSerializer = Json {
    serializersModule = SerializersModule {
        contextual(ExceptionSerializer)
    }
    prettyPrint = true
}