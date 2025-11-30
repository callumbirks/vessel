package com.doofcraft.vessel.common.component

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.item.UseAnim

data class ConsumableComponent(
    val animation: UseAnim, val duration: Int
) {
    companion object {
        val CODEC: Codec<ConsumableComponent> = RecordCodecBuilder.create { record ->
            record.group(
                Codec.STRING.xmap({ useAnim(it) }, { useAnimString(it) }).fieldOf("animation").forGetter(
                    ConsumableComponent::animation
                ), Codec.INT.fieldOf("duration").forGetter(ConsumableComponent::duration)
            ).apply(record, ::ConsumableComponent)
        }

        fun useAnim(string: String): UseAnim {
            return when (string) {
                "none" -> UseAnim.NONE
                "eat" -> UseAnim.EAT
                "drink" -> UseAnim.DRINK
                "block" -> UseAnim.BLOCK
                "bow" -> UseAnim.BOW
                "spear" -> UseAnim.SPEAR
                "crossbow" -> UseAnim.CROSSBOW
                "spyglass" -> UseAnim.SPYGLASS
                "toot_horn" -> UseAnim.TOOT_HORN
                "brush" -> UseAnim.BRUSH
                else -> error { "No such UseAnim '$string'" }
            }
        }

        fun useAnimString(anim: UseAnim): String {
            return when (anim) {
                UseAnim.NONE -> "none"
                UseAnim.EAT -> "eat"
                UseAnim.DRINK -> "drink"
                UseAnim.BLOCK -> "block"
                UseAnim.BOW -> "bow"
                UseAnim.SPEAR -> "spear"
                UseAnim.CROSSBOW -> "crossbow"
                UseAnim.SPYGLASS -> "spyglass"
                UseAnim.TOOT_HORN -> "toot_horn"
                UseAnim.BRUSH -> "brush"
            }
        }
    }
}
