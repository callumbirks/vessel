package com.doofcraft.vessel.api

import com.doofcraft.vessel.component.VesselTag
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.FoodComponent
import net.minecraft.component.type.FoodComponents
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.TypedActionResult

abstract class VesselFood(tag: VesselTag, food: FoodComponent = FoodComponents.BREAD): VesselItem(tag) {
    init {
        addComponent(DataComponentTypes.FOOD, food)
    }
}