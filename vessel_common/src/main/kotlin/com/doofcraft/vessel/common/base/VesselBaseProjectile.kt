package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.component.ProjectileData
import com.doofcraft.vessel.common.registry.ModItems
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

open class VesselBaseProjectile : ThrowableItemProjectile {
    private val data: ProjectileData

    constructor(data: ProjectileData, level: Level, shooter: LivingEntity) : super(
        EntityType.SNOWBALL, shooter, level
    ) {
        this.data = data
    }

    constructor(data: ProjectileData, level: Level, x: Double, y: Double, z: Double) : super(
        EntityType.SNOWBALL, x, y, z, level
    ) {
        this.data = data
    }

    override fun getDefaultItem(): Item {
        return ModItems.PROJECTILE_ITEM
    }

    private fun getParticle(): ParticleOptions {
        return if (!item.isEmpty && !item.`is`(defaultItem)) ItemParticleOption(ParticleTypes.ITEM, item)
        else ParticleTypes.ITEM_SNOWBALL
    }

    override fun handleEntityEvent(id: Byte) {
        // ID 3 is entity ded. This is what spawns little snowballs when it bursts
        if (id.toInt() == 3) {
            val particleOptions = getParticle()

            repeat(8) {
                level().addParticle(particleOptions, x, y, z, 0.0, 0.0, 0.0)
            }
        }
    }

    override fun onHitEntity(result: EntityHitResult) {
        super.onHitEntity(result)
        if (data.damage > 0f) {
            result.entity.hurt(damageSources().thrown(this, this.owner), data.damage)
        }
    }

    override fun onHit(result: HitResult) {
        super.onHit(result)
        if (!level().isClientSide) {
            level().broadcastEntityEvent(this, 3.toByte())
            this.discard()
        }
    }
}