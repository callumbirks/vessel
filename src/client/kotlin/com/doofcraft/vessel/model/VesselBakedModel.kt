package com.doofcraft.vessel.model

import com.doofcraft.vessel.util.collections.LruCache
import com.doofcraft.vessel.util.hash.Fnv64
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponentType
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

@Environment(EnvType.CLIENT)
class VesselBakedModel(
    private val fallback: BakedModel,
    private val overrides: List<VesselBakedOverride>,
) : BakedModel {
    private val componentIds = overrides.flatMap { it.predicate.componentList() }.toSet()
    private val components: List<DataComponentType<Any>> = componentIds.mapNotNull { resolveComponent(it) }
    private val cache = LruCache<Long, BakedModel>(1024)

    private var lastTime = 0L
    private var timer = 0L
    private var counter = 0
    private var hitCounter = 0
    private var missCounter = 0

    override fun getQuads(state: BlockState?, direction: Direction?, random: RandomSource): List<BakedQuad> {
        return fallback.getQuads(state, direction, random)
    }

    override fun useAmbientOcclusion(): Boolean = fallback.useAmbientOcclusion()

    override fun isGui3d(): Boolean {
        return fallback.isGui3d
    }

    override fun usesBlockLight(): Boolean {
        return fallback.usesBlockLight()
    }

    override fun isCustomRenderer(): Boolean {
        return false
    }

    override fun getParticleIcon(): TextureAtlasSprite {
        return fallback.particleIcon
    }

    override fun getTransforms(): ItemTransforms {
        return fallback.transforms
    }

    override fun getOverrides() = object : ItemOverrides() {
        override fun resolve(
            model: BakedModel,
            stack: ItemStack,
            level: ClientLevel?,
            entity: LivingEntity?,
            seed: Int
        ): BakedModel {
            val now = System.currentTimeMillis()
            if (now - lastTime >= 1000L) {
                lastTime = now
                counter = 1
                hitCounter = 0
                missCounter = 0
                timer = 0
            } else {
                counter++
            }

            val hasher = Fnv64()
            for (component in components) {
                if (component.codec() == null) {
                    hasher.updateJson(null)
                    continue
                }
                val value = stack.get(component)
                if (value == null) {
                    hasher.updateJson(null)
                    continue
                }
                val json = toJson(component.codec()!!, value)
                hasher.updateJson(json)
            }
            val r = hasher.value()

            cache[r]?.let {
                hitCounter++
                timer += (System.currentTimeMillis() - now)
                return it
            }

            missCounter++

            val override = overrides.firstOrNull { it.predicate.test(stack) }
            val result = override?.model ?: fallback
            cache[r] = result
            timer += (System.currentTimeMillis() - now)
            return result
        }
    }
}