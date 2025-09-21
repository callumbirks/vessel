package com.doofcraft.vessel.base

import com.doofcraft.vessel.VesselMod
import com.doofcraft.vessel.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

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
        setChanged()
    }

    fun updateItem(fn: (ItemStack) -> Unit): ItemStack {
        fn.invoke(item)
        val newItem = item.copyWithCount(1)
        item = newItem
        setChanged()
        return item
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        item = tag.getCompound("item").let { ItemStack.parse(registries, it).orElse(ItemStack.EMPTY) }
        yaw = tag.getFloat("yaw")
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        if (!item.isEmpty) tag.put("item", item.save(registries))
        tag.putFloat("yaw", yaw)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag? {
        val tag = super.getUpdateTag(registries)
        saveAdditional(tag, registries)
        return tag
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener?>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }
}