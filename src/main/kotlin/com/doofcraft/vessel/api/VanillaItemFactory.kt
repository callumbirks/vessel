package com.doofcraft.vessel.api

import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack

class VanillaItemFactory(val item: ItemConvertible): ItemStackFactory {
    override fun create(count: Int): ItemStack {
        return ItemStack(item, count)
    }
}