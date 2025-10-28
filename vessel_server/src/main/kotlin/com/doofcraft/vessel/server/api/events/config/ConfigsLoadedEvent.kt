package com.doofcraft.vessel.server.api.events.config

import net.minecraft.resources.ResourceLocation

data class ConfigsLoadedEvent(val ids: List<ResourceLocation>)
