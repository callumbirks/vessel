package com.doofcraft.vessel.common.api.item

import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.food.Foods
import net.minecraft.world.item.ItemStack

abstract class VesselFood(tag: VesselTag, food: FoodProperties = Foods.BREAD): VesselItem(tag) {
    init {
        addComponent(DataComponents.FOOD) { food }
    }

    override val baseItem = ModItems.ITEM

    // `use` cannot be used on Foods, because we will defer to the Minecraft methods for consuming food.
    // Extra behaviours should be added to `finishUsing`.
    final override fun use(
        stack: ItemStack,
        level: ServerLevel,
        player: ServerPlayer,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        return InteractionResultHolder.pass(stack)
    }
}