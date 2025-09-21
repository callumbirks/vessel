package com.doofcraft.vessel.base

import com.doofcraft.vessel.component.VesselTag
import com.doofcraft.vessel.registry.ModBlocks
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack

class VesselBaseBlockItem: BlockItem(ModBlocks.VESSEL, Properties()) {
    override fun getDescriptionId(stack: ItemStack): String {
        val tag = stack.get(VesselTag.COMPONENT) ?: return super.getDescriptionId()
        return "item.${tag.key}"
    }
}