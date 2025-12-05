package com.doofcraft.vessel.server.api

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.common.api.item.ItemStackFactory
import com.doofcraft.vessel.common.api.item.VanillaItemFactory
import com.doofcraft.vessel.common.api.item.Vessel
import com.doofcraft.vessel.common.api.item.VesselBlock
import com.doofcraft.vessel.common.api.item.VesselItem
import com.doofcraft.vessel.common.component.VesselTag
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

object VesselRegistry {
    private val items = hashMapOf<String, Vessel>()

    @JvmStatic
    fun get(key: String): Vessel? = items[key]

    @JvmStatic
    fun all(): Collection<Vessel> = items.values

    inline fun <reified T : Vessel> getOfType(key: String): T? = get(key) as? T

    @JvmStatic
    fun getBlock(key: String): VesselBlock? = items[key] as? VesselBlock

    @JvmStatic
    fun getItem(key: String): VesselItem? = items[key] as? VesselItem

    fun find(key: String): ItemStackFactory? {
        if (key.contains(':')) {
            return find(ResourceLocation.parse(key))
        }
        return items[key] ?: BuiltInRegistries.ITEM.getHolder(ResourceLocation.withDefaultNamespace(key))
            .getOrNull()
            ?.let { VanillaItemFactory(it.value()) }
    }

    fun find(key: ResourceLocation): ItemStackFactory? {
        if (key.namespace == VesselMod.MODID) {
            return items[key.path]
        }
        return BuiltInRegistries.ITEM.getHolder(key).getOrNull()?.let { VanillaItemFactory(it.value()) }
    }

    fun find(key: VesselIdentifier): ItemStackFactory? {
        return when (key.namespace) {
            null, VesselMod.MODID -> items[key.path]
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
            return VesselIdentifier.Companion.of(id.namespace, id.path)
        }
        return VesselIdentifier.Companion.vessel(tag.key)
    }

    fun <T : VesselItem> register(item: T): T {
        items[item.tag.key] = item
        return item
    }

    fun remove(key: String) {
        items.remove(key)
        // Automatically remove associated behaviour
        VesselBehaviourRegistry.remove(key)
    }

    inline fun <reified T : Vessel> removeTyped() {
        all().filterIsInstance<T>().forEach { remove(it.tag.key) }
    }
}