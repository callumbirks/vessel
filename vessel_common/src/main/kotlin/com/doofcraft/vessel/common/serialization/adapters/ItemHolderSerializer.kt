package com.doofcraft.vessel.common.serialization.adapters

import net.minecraft.core.Holder
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object ItemHolderSerializer : CodecSerializer<Holder<Item>>("net.minecraft.Holder<Item>", ItemStack.ITEM_NON_AIR_CODEC)