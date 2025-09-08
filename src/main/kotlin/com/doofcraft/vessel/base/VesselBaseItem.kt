package com.doofcraft.vessel.base

import com.doofcraft.vessel.component.VesselTag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class VesselBaseItem(): Item(Settings()) {
    override fun getTranslationKey(stack: ItemStack): String {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getTranslationKey()
        return "item.${tag.key}"
    }
}