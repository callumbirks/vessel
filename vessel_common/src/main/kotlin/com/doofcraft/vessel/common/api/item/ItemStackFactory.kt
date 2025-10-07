package com.doofcraft.vessel.common.api.item

import net.minecraft.world.item.ItemStack

interface ItemStackFactory {
    fun create(count: Int): ItemStack
}