package com.doofcraft.vessel.common.registry

import com.doofcraft.vessel.common.component.AnimatedUseComponent
import com.doofcraft.vessel.common.component.BlockShapeComponent
import com.doofcraft.vessel.common.component.ProjectileData
import com.doofcraft.vessel.common.component.ProjectileWeaponData
import com.doofcraft.vessel.common.predicate.VesselPredicate
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries

object BehaviourComponents : SimpleRegistry<Registry<DataComponentType<*>>, DataComponentType<*>>() {
    override val registry: Registry<DataComponentType<*>> = BuiltInRegistries.DATA_COMPONENT_TYPE


    @JvmField
    val BLOCK_SHAPE: DataComponentType<BlockShapeComponent> = create(
        "block_shape", DataComponentType.builder<BlockShapeComponent>().persistent(BlockShapeComponent.CODEC).build()
    )

    @JvmField
    val ANIMATED_USE: DataComponentType<AnimatedUseComponent> = create(
        "item_use", DataComponentType.builder<AnimatedUseComponent>().persistent(AnimatedUseComponent.CODEC).build()
    )

    @JvmField
    val PREDICATE: DataComponentType<VesselPredicate> = create(
        "predicate", DataComponentType.builder<VesselPredicate>().persistent(VesselPredicate.CODEC).build()
    )

    @JvmField
    val PROJECTILE_WEAPON: DataComponentType<ProjectileWeaponData> = create(
        "projectile_weapon",
        DataComponentType.builder<ProjectileWeaponData>().persistent(ProjectileWeaponData.CODEC).build()
    )

    @JvmField
    val PROJECTILE: DataComponentType<ProjectileData> = create(
        "projectile",
        DataComponentType.builder<ProjectileData>().persistent(ProjectileData.CODEC).build()
    )
}