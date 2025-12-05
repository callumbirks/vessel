package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.base.VesselBaseBlockItem
import com.doofcraft.vessel.common.base.VesselBaseConsumable
import com.doofcraft.vessel.common.base.VesselBaseItem
import com.doofcraft.vessel.common.base.VesselBaseProjectileWeapon
import com.doofcraft.vessel.common.base.VesselBaseTool
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.Item

object ModItems : SimpleRegistry<Registry<Item>, Item>() {
    override val registry: Registry<Item> = BuiltInRegistries.ITEM

    val ITEM = create("item", VesselBaseItem())
    val CONSUMABLE = create("consumable", VesselBaseConsumable())
    val BLOCK_ITEM = create("block_item", VesselBaseBlockItem())
    val TOOL = create("tool", VesselBaseTool())
    val PROJECTILE_WEAPON = create("projectile_weapon", VesselBaseProjectileWeapon())
}