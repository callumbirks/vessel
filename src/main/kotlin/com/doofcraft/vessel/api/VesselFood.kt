package com.doofcraft.vessel.api

import com.doofcraft.vessel.component.VesselTag
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.FoodComponent
import net.minecraft.component.type.FoodComponents

abstract class VesselFood(tag: VesselTag, food: FoodComponent = FoodComponents.BREAD): VesselItem(tag) {
    init {
        addComponent(DataComponentTypes.FOOD) { food }
    }
}