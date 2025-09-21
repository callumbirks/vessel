package com.doofcraft.vessel.api

import com.doofcraft.vessel.VesselMod
import com.doofcraft.vessel.component.VesselTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

object VesselRegistry {
    private val blocks = hashMapOf<String, VesselBlock>()
    private val items = hashMapOf<String, VesselItem>()

    fun getBlock(key: String): VesselBlock? = blocks[key]

    fun getBlockOrThrow(key: String): VesselBlock =
        blocks[key] ?: throw NoSuchElementException("No such VesselBlock '$key'")

    fun getItem(key: String): VesselItem? = items[key]

    fun getItemOrThrow(key: String): VesselItem =
        items[key] ?: throw NoSuchElementException("No such VesselItem '$key'")

    fun listItems(): List<VesselItem> = items.values.toList()

    fun listBlocks(): List<VesselBlock> = blocks.values.toList()

    fun find(key: String): ItemStackFactory? {
        if (key.contains(':')) {
            return find(ResourceLocation.parse(key))
        }
        return items[key] ?: blocks[key] ?: BuiltInRegistries.ITEM.getHolder(ResourceLocation.withDefaultNamespace(key))
            .getOrNull()
            ?.let { VanillaItemFactory(it.value()) }
    }

    fun find(key: ResourceLocation): ItemStackFactory? {
        if (key.namespace == VesselMod.MODID) {
            return items[key.path] ?: blocks[key.path]
        }
        return BuiltInRegistries.ITEM.getHolder(key).getOrNull()?.let { VanillaItemFactory(it.value()) }
    }

    fun find(key: VesselIdentifier): ItemStackFactory? {
        return when (key.namespace) {
            null, VesselMod.MODID -> items[key.path] ?: blocks[key.path]
            else -> BuiltInRegistries.ITEM.getHolder(key.toIdentifier())
                .getOrNull()
                ?.let { VanillaItemFactory(it.value()) }
        }
    }

    fun match(stack: ItemStack): VesselIdentifier {
        val tag = stack.get(VesselTag.COMPONENT)
        if (tag == null) {
            val id = stack.itemHolder.unwrapKey().getOrNull()?.location() ?: ResourceLocation.withDefaultNamespace("air")
            return VesselIdentifier.of(id.namespace, id.path)
        }
        return VesselIdentifier.vessel(tag.key)
    }

    fun <T : VesselBlock> addBlock(block: T): T {
        blocks[block.tag.key] = block
        return block
    }

    fun <T : VesselItem> addItem(item: T): T {
        items[item.tag.key] = item
        return item
    }

    fun removeBlock(key: String) {
        blocks.remove(key)
    }

    fun removeItem(key: String) {
        items.remove(key)
    }
}