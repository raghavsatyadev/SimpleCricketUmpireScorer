package io.github.raghavsatyadev.support.models.essential

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class CustomError(
    val errorCode: ErrorCode,
    @Contextual
    val exception: Exception? = null,
)