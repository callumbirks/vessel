package com.doofcraft.vessel.api

import net.minecraft.item.ItemStack

interface ItemStackFactory {
    fun create(count: Int): ItemStack
}