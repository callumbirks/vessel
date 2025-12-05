package com.doofcraft.vessel.common.serialization.adapters

import net.minecraft.world.item.ItemStack

object ItemStackSerializer : CodecSerializer<ItemStack>("net.minecraft.ItemStack", ItemStack.SINGLE_ITEM_CODEC)