package com.doofcraft.vessel.api

import com.doofcraft.vessel.component.VesselTag
import net.minecraft.core.component.DataComponents
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.food.Foods

abstract class VesselFood(tag: VesselTag, food: FoodProperties = Foods.BREAD): VesselItem(tag) {
    init {
        addComponent(DataComponents.FOOD) { food }
    }
}