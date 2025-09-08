package com.doofcraft.vessel.base

import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.registry.ModBlocks
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack

class VesselBaseBlockItem: BlockItem(ModBlocks.VESSEL, Settings()) {
    override fun getTranslationKey(stack: ItemStack): String {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getTranslationKey()
        return "item.${tag.key}"
    }
}