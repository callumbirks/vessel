package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import net.minecraft.world.item.crafting.Ingredient

data class IngredientComponent(val ingredient: Ingredient) {
    companion object {
        val CODEC: Codec<IngredientComponent> = Ingredient.CODEC.xmap({ IngredientComponent(it) }, { it.ingredient })
    }
}
