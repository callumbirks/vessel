package com.doofcraft.vessel.common.api.event

import com.doofcraft.vessel.common.api.item.Vessel
import net.minecraft.world.item.ItemStack

data class ItemStackCreatedEvent(
    val item: Vessel,
    val stack: ItemStack,
)
