package io.github.raghavsatyadev.support.models.essential

import kotlinx.serialization.Serializable

@Serializable
class GeneralModel<T>(var data: T)