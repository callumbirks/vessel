package com.doofcraft.vessel.common.base

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.component.VesselTag
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.registry.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentType
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class VesselBaseBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(ModBlockEntities.VESSEL, pos, state) {
    var item: ItemStack = ItemStack.EMPTY
        private set

    val tag: VesselTag
        get() = item.get(VesselTag.COMPONENT)!!

    var yaw: Float = 0f
    var shape: VoxelShape? = null

    fun isInitialized(): Boolean = !item.isEmpty

    fun initialize(item: ItemStack, yaw: Float = 0f) {
        if (!this.item.isEmpty) {
            VesselMod.LOGGER.warn("BlockEntity initialized multiple times")
        }
        this.item = item.copyWithCount(1)
        this.yaw = yaw

        val shape = VesselBehaviourRegistry.get(tag.key, BehaviourComponents.BLOCK_SHAPE)
        if (shape != null && shape.isValid()) {
            this.shape = Shapes.create(shape.x1, shape.y1, shape.z1, shape.x2, shape.y2, shape.z2)
        }

        setChangedAndSync()

        VesselBehaviourRegistry.CHANGED.subscribe { map ->
            val components = map[tag.key] ?: return@subscribe
            val shape = components.get(BehaviourComponents.BLOCK_SHAPE) ?: return@subscribe
            if (shape.isValid()) {
                this.shape = Shapes.create(shape.x1, shape.y1, shape.z1, shape.x2, shape.y2, shape.z2)
                setChangedAndSync()
            }
        }
    }

    fun <T> has(component: DataComponentType<T>): Boolean = item.has(component)

    fun <T> get(component: DataComponentType<T>): T? = item.get(component)

    fun <T> set(component: DataComponentType<T>, value: T): T? {
        val result = item.set(component, value)
        setItemChanged()
        return result
    }

    fun <T> remove(component: DataComponentType<T>): T? {
        val result = item.remove(component)
        setItemChanged()
        return result
    }

    /**
     * Must be used whenever `item` changes, otherwise changes will not sync properly.
     */
    fun setItemChanged() {
        item = item.copyWithCount(1)
        if (level?.isLoaded(blockPos) == true) {
            setChangedAndSync()
        }
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

    fun setChangedAndSync() {
        setChanged()
        level?.sendBlockUpdated(worldPosition, blockState, blockState, Block.UPDATE_CLIENTS)
    }
}