package com.doofcraft.vessel.client.serialization

import com.doofcraft.vessel.common.serialization.ResourceLocationSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val vesselSerializationModule = SerializersModule {
    contextual(ResourceLocationSerializer)
}