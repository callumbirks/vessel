package com.doofcraft.vessel.common.util

import com.doofcraft.vessel.common.component.VesselTag
import net.minecraft.world.item.ItemStack

fun ItemStack.vesselTag(): VesselTag? = get(VesselTag.COMPONENT)