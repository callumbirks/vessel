package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.base.VesselBaseArmor
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.world.item.ArmorItem

abstract class VesselArmor(
    tag: VesselTag, val armorType: ArmorItem.Type
) : VesselItem(tag) {
    override val baseItem: VesselBaseArmor
        get() = when (armorType) {
            ArmorItem.Type.HELMET -> ModItems.ARMOR_HELMET
            ArmorItem.Type.CHESTPLATE -> ModItems.ARMOR_CHESTPLATE
            ArmorItem.Type.LEGGINGS -> ModItems.ARMOR_LEGGINGS
            ArmorItem.Type.BOOTS -> ModItems.ARMOR_BOOTS
            ArmorItem.Type.BODY -> error { "unsupported armor type 'body'" }
        }
}