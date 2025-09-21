package com.doofcraft.vessel.base

import com.doofcraft.vessel.component.VesselTag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class VesselBaseItem(): Item(Properties()) {
    override fun getDescriptionId(stack: ItemStack): String {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getDescriptionId()
        return "item.${tag.key}"
    }
}