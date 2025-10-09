package com.doofcraft.vessel.client.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val vesselSerializationModule = SerializersModule {
    contextual(ResourceLocationSerializer)
}