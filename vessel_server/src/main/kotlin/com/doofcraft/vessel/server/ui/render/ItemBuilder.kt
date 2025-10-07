package com.doofcraft.vessel.server.ui.render

import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.common.api.item.ItemStackFactory
import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.common.registry.ModComponents
import com.doofcraft.vessel.server.api.VesselRegistry
import com.doofcraft.vessel.server.util.toText
import de.themoep.minedown.adventure.MineDown
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.CommonColors
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

class ItemBuilder(val icon: ItemStackFactory) {
    private var name: MineDown? = null
    private var lore: List<MineDown> = emptyList()

    fun withName(name: String?): ItemBuilder {
        this.name = name?.let { MineDown(it).replaceFirst(true) }
        return this
    }

    fun withLore(lore: List<String>): ItemBuilder {
        this.lore = lore.map { MineDown(it).replaceFirst(true) }
        return this
    }

    fun build(
        nameReplacements: MineDown.() -> Unit = {},
        loreReplacements: MineDown.() -> Unit = {},
        button: MenuButton? = null
    ): ItemStack {
        val nameMd = this.name?.copy()
        val loreMd = this.lore.map { it.copy() }.toList()
        nameMd?.let { nameReplacements(it) }
        loreMd.forEach(loreReplacements)

        val itemStack = icon.create(1)
        nameMd?.let {
            itemStack.set(DataComponents.ITEM_NAME, it.toComponent().toText())
        }
        if (loreMd.isNotEmpty()) {
            itemStack.set(DataComponents.LORE, ItemLore(loreMd.map {
                it.toComponent().toText().copy().withStyle {
                    it.withColor(
                        CommonColors.WHITE
                    ).withItalic(false)
                }
            }))
        }
        if (button != null) {
            itemStack.set(ModComponents.MENU_BUTTON, button)
        }
        return itemStack
    }

    companion object {
        fun of(key: String): ItemBuilder {
            return ItemBuilder(VesselRegistry.findOrThrow(key))
        }

        fun of(namespace: String, path: String): ItemBuilder {
            return ItemBuilder(VesselRegistry.findOrThrow(ResourceLocation.fromNamespaceAndPath(namespace, path)))
        }

        fun of(identifier: ResourceLocation): ItemBuilder {
            return ItemBuilder(VesselRegistry.findOrThrow(identifier))
        }

        fun of(identifier: VesselIdentifier): ItemBuilder {
            return ItemBuilder(VesselRegistry.findOrThrow(identifier))
        }
    }
}