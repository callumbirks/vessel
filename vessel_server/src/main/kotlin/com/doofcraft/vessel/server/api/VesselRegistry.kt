package com.doofcraft.vessel.server.api

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.api.item.ItemStackFactory
import com.doofcraft.vessel.common.api.item.VanillaItemFactory
import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.common.api.item.VesselBlock
import com.doofcraft.vessel.common.api.item.VesselItem
import com.doofcraft.vessel.common.api.item.VesselTool
import com.doofcraft.vessel.common.component.VesselTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

object VesselRegistry {
    private val blocks = hashMapOf<String, VesselBlock>()
    private val items = hashMapOf<String, VesselItem>()
    private val tools = hashMapOf<String, VesselTool>()

    @JvmStatic
    fun getBlock(key: String): VesselBlock? = blocks[key]

    inline fun <reified T : VesselBlock> getBlockOfType(key: String): T? = getBlock(key) as? T

    @JvmStatic
    fun getBlockOrThrow(key: String): VesselBlock =
        blocks[key] ?: throw NoSuchElementException("No such VesselBlock '$key'")

    @JvmStatic
    fun getItem(key: String): VesselItem? = items[key]

    inline fun <reified T : VesselItem> getItemOfType(key: String): T? = getItem(key) as? T

    @JvmStatic
    fun getItemOrThrow(key: String): VesselItem =
        items[key] ?: throw NoSuchElementException("No such VesselItem '$key'")

    @JvmStatic
    fun getTool(key: String): VesselTool? = tools[key]

    inline fun <reified T : VesselTool> getToolOfType(key: String): T? = getTool(key) as? T

    @JvmStatic
    fun getToolOrThrow(key: String): VesselTool =
        tools[key] ?: throw NoSuchElementException("No such VesselTool '$key'")

    fun listItems(): List<VesselItem> = items.values.toList()

    fun listBlocks(): List<VesselBlock> = blocks.values.toList()

    fun listTools(): List<VesselTool> = tools.values.toList()

    fun find(key: String): ItemStackFactory? {
        if (key.contains(':')) {
            return find(ResourceLocation.parse(key))
        }
        return items[key] ?: blocks[key] ?: tools[key]
        ?: BuiltInRegistries.ITEM.getHolder(ResourceLocation.withDefaultNamespace(key))
            .getOrNull()
            ?.let { VanillaItemFactory(it.value()) }
    }

    fun find(key: ResourceLocation): ItemStackFactory? {
        if (key.namespace == VesselMod.MODID) {
            return items[key.path] ?: blocks[key.path] ?: tools[key.path]
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

    fun findOrThrow(key: String): ItemStackFactory {
        return find(key) ?: throw NoSuchElementException("No such item '$key'")
    }

    fun findOrThrow(key: ResourceLocation): ItemStackFactory {
        return find(key) ?: throw NoSuchElementException("No such item '$key'")
    }

    fun findOrThrow(key: VesselIdentifier): ItemStackFactory {
        return find(key) ?: throw NoSuchElementException("No such item '$key'")
    }

    fun match(stack: ItemStack): VesselIdentifier {
        val tag = stack.get(VesselTag.Companion.COMPONENT)
        if (tag == null) {
            val id =
                stack.itemHolder.unwrapKey().getOrNull()?.location() ?: ResourceLocation.withDefaultNamespace("air")
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

    fun <T : VesselTool> addTool(tool: T): T {
        tools[tool.tag.key] = tool
        return tool
    }

    fun removeBlock(key: String) {
        blocks.remove(key)
    }

    fun removeItem(key: String) {
        items.remove(key)
    }

    fun removeTool(key: String) {
        tools.remove(key)
    }

    fun <T : VesselBlock> removeBlocks(clazz: Class<T>) {
        for (key in blocks.filterValues { clazz.isInstance(it) }.keys) {
            blocks.remove(key)
        }
    }

    fun <T : VesselItem> removeItems(clazz: Class<T>) {
        for (key in items.filterValues { clazz.isInstance(it) }.keys) {
            items.remove(key)
        }
    }

    fun <T : VesselTool> removeTools(clazz: Class<T>) {
        for (key in tools.filterValues { clazz.isInstance(it) }.keys) {
            tools.remove(key)
        }
    }
}