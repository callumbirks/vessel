package com.doofcraft.vessel.common.api.item

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike

class VanillaItemFactory(val item: ItemLike): ItemStackFactory {
    override fun create(count: Int): ItemStack {
        return ItemStack(item, count)
    }
}