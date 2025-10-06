package com.doofcraft.vessel.api.item

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

class VanillaItemFactory(val item: ItemLike): com.doofcraft.vessel.api.ItemStackFactory {
    override fun create(count: Int): ItemStack {
        return ItemStack(item, count)
    }
}