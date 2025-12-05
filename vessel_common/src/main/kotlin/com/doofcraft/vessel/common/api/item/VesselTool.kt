package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.component.IngredientComponent
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.StackComponents
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.core.component.DataComponents
import net.minecraft.tags.TagKey
import net.minecraft.world.item.DiggerItem
import net.minecraft.world.item.Tier
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.block.Block

abstract class VesselTool(
    tag: VesselTag, val tier: Tier, val blocks: TagKey<Block>, modifiers: ItemAttributeModifiers
) : VesselItem(tag) {
    constructor(tag: VesselTag, tier: Tier, blocks: TagKey<Block>, attackDamage: Float, attackSpeed: Float) : this(
        tag, tier, blocks, DiggerItem.createAttributes(tier, attackDamage, attackSpeed)
    )

    override val baseItem = ModItems.TOOL

    init {
        addComponent(DataComponents.TOOL) { tier.createToolProperties(blocks) }
        addComponent(DataComponents.ATTRIBUTE_MODIFIERS) { modifiers }
        addComponent(StackComponents.INGREDIENT) { IngredientComponent(tier.repairIngredient) }
        addComponent(DataComponents.MAX_DAMAGE) { 100 }
    }
}