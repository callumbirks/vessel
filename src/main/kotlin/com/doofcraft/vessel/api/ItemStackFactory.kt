package com.doofcraft.vessel.api

import net.minecraft.world.item.ItemStack

interface ItemStackFactory {
    fun create(count: Int): ItemStack
}