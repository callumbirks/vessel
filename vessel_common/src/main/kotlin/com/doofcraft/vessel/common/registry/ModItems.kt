package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.base.VesselBaseArmor
import com.doofcraft.vessel.common.base.VesselBaseBlockItem
import com.doofcraft.vessel.common.base.VesselBaseItem
import com.doofcraft.vessel.common.base.VesselBaseProjectileItem
import com.doofcraft.vessel.common.base.VesselBaseProjectileWeapon
import com.doofcraft.vessel.common.base.VesselBaseTool
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.Item

object ModItems : SimpleRegistry<Registry<Item>, Item>() {
    override val registry: Registry<Item> = BuiltInRegistries.ITEM

    val ITEM = create("item", VesselBaseItem())
    val BLOCK_ITEM = create("block_item", VesselBaseBlockItem())
    val TOOL = create("tool", VesselBaseTool())
    val PROJECTILE_WEAPON = create("projectile_weapon", VesselBaseProjectileWeapon())
    val PROJECTILE_ITEM = create("projectile", VesselBaseProjectileItem())
    val ARMOR_HELMET = create(
        "armor_helmet", VesselBaseArmor(
            ArmorMaterials.NETHERITE,
            ArmorItem.Type.HELMET,
            Item.Properties().fireResistant().durability(ArmorItem.Type.HELMET.getDurability(37))
        )
    )
    val ARMOR_CHESTPLATE = create(
        "armor_chestplate", VesselBaseArmor(
            ArmorMaterials.NETHERITE,
            ArmorItem.Type.CHESTPLATE,
            Item.Properties().fireResistant().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))
        )
    )
    val ARMOR_LEGGINGS = create(
        "armor_leggings", VesselBaseArmor(
            ArmorMaterials.NETHERITE,
            ArmorItem.Type.LEGGINGS,
            Item.Properties().fireResistant().durability(ArmorItem.Type.LEGGINGS.getDurability(37))
        )
    )
    val ARMOR_BOOTS = create(
        "armor_boots", VesselBaseArmor(
            ArmorMaterials.NETHERITE,
            ArmorItem.Type.BOOTS,
            Item.Properties().fireResistant().durability(ArmorItem.Type.BOOTS.getDurability(37))
        )
    )
}