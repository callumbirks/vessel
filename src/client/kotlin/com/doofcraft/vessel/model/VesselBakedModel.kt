package com.doofcraft.vessel.model

import com.doofcraft.vessel.VesselMod
import com.doofcraft.vessel.util.collections.LruCache
import com.doofcraft.vessel.util.hash.Fnv64
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random

@Environment(EnvType.CLIENT)
class VesselBakedModel(
    private val fallback: BakedModel,
    private val overrides: List<VesselBakedOverride>,
) : BakedModel {
    private val componentIds = overrides.flatMap { it.predicate.componentList() }.toSet()
    private val components = componentIds.mapNotNull { resolveComponent(it) }
    private val cache = LruCache<Long, BakedModel>(1024)

    private var lastTime = 0L
    private var timer = 0L
    private var counter = 0
    private var hitCounter = 0
    private var missCounter = 0

    override fun getQuads(
        state: BlockState?, face: Direction?, random: Random
    ): List<BakedQuad> = fallback.getQuads(state, face, random)

    override fun useAmbientOcclusion(): Boolean = fallback.useAmbientOcclusion()

    override fun hasDepth(): Boolean = fallback.hasDepth()

    override fun isSideLit(): Boolean = fallback.isSideLit

    override fun isBuiltin(): Boolean = false

    override fun getParticleSprite(): Sprite = fallback.particleSprite

    override fun getTransformation(): ModelTransformation = fallback.transformation

    override fun getOverrides(): ModelOverrideList = object : ModelOverrideList() {
        override fun apply(
            model: BakedModel, stack: ItemStack, world: ClientWorld?, entity: LivingEntity?, seed: Int
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
                if (component.codec == null) {
                    hasher.updateJson(null)
                    continue
                }
                val value = stack.get(component)
                if (value == null) {
                    hasher.updateJson(null)
                    continue
                }
                val json = toJson(component.codec!!, value)
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