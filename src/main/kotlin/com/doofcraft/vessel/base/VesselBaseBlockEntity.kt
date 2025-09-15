package com.doofcraft.vessel.base

import com.doofcraft.vessel.VesselMod
import com.doofcraft.vessel.registry.ModBlockEntities
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.math.BlockPos

class VesselBaseBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(ModBlockEntities.VESSEL, pos, state) {
    var item: ItemStack = ItemStack.EMPTY
        private set

    var yaw: Float = 0f

    fun initialize(item: ItemStack, yaw: Float = 0f) {
        if (!this.item.isEmpty) {
            VesselMod.LOGGER.warn("BlockEntity initialized multiple times")
        }
        this.item = item.copyWithCount(1)
        this.yaw = yaw
        markDirtyAndSync()
    }

    override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.writeNbt(nbt, registryLookup)
        if (!item.isEmpty) nbt.put("item", item.encode(registryLookup))
        nbt.putFloat("yaw", yaw)
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        super.readNbt(nbt, registryLookup)
        item = nbt.getCompound("item").let { ItemStack.fromNbt(registryLookup, it).orElse(ItemStack.EMPTY) }
        yaw = nbt.getFloat("yaw")
    }

    override fun toInitialChunkDataNbt(registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
        return NbtCompound().also { writeNbt(it, registryLookup) }
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener> {
        super.toUpdatePacket()
        return BlockEntityUpdateS2CPacket.create(this)
    }

    private fun markDirtyAndSync() {
        markDirty()
        world?.updateListeners(pos, cachedState, cachedState, 3)
    }
}