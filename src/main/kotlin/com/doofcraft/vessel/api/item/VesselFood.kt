package com.doofcraft.vessel.api.item

import com.doofcraft.vessel.component.VesselTag
import net.minecraft.core.component.DataComponents
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.food.Foods

abstract class VesselFood(tag: VesselTag, food: FoodProperties = Foods.BREAD): com.doofcraft.vessel.api.item.VesselItem(tag) {
    init {
        _root_ide_package_.com.doofcraft.vessel.api.item.Vessel.addComponent(DataComponents.FOOD) { food }
    }
}