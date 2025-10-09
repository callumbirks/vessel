package com.doofcraft.vessel.server.ui.render

import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.common.registry.ModComponents
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.JsonTemplater
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.model.IconDef
import com.doofcraft.vessel.server.ui.model.MenuDefinition
import com.doofcraft.vessel.server.ui.model.WidgetDef
import com.doofcraft.vessel.server.ui.text.ComponentFactory
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack


data class RenderedMenu(
    val items: Map<Int, ItemStack>
)

class WidgetRenderer(
    private val engine: ExprEngine
) {
    fun renderAll(
        def: MenuDefinition,
        title: String,
        nodeValues: Map<String, Any?>,
        state: Map<String, Any?>,
        player: ServerPlayer,
    ): RenderedMenu {
        val out = HashMap<Int, ItemStack>()
        val scopeBase = Scope(
            menu = mapOf("id" to def.id, "title" to title, "rows" to def.rows),
            player = mapOf("uuid" to player.uuid.toString(), "name" to player.scoreboardName),
            nodeValues = nodeValues,
            state = state,
        )

        fun renderIcon(icon: IconDef, scope: Scope, player: ServerPlayer): ItemStack {
            val itemIdStr = engine.renderTemplate(icon.item, scope)
            val itemId = VesselIdentifier.parse(itemIdStr)

            val name = icon.name?.let { engine.renderTemplate(it, scope) }
            val lore = icon.lore?.map { engine.renderTemplate(it, scope) } ?: emptyList()

            val builder = ItemBuilder.of(itemId).withName(name).withLore(lore)

            val nameRepl = icon.replacements?.name ?: emptyMap()
            val loreRepl = icon.replacements?.lore ?: emptyMap()

            val stack: ItemStack = builder.build(nameReplacements = {
                nameRepl.forEach { (placeholder, spec) ->
                    val comp = ComponentFactory.build(spec, engine, scope)
                    replace(placeholder, comp)
                }
            }, loreReplacements = {
                loreRepl.forEach { (placeholder, spec) ->
                    val comp = ComponentFactory.build(spec, engine, scope)
                    replace(placeholder, comp)
                }
            })

            for (m in UiComponentMappers.all()) {
                if (!m.shouldApply(itemId, stack, scope, player)) continue
                m.apply(itemId, stack, scope, player)
            }

            return stack
        }

        def.widgets.forEach { w ->
            when (w) {
                is WidgetDef.Button -> {
                    val enabled =
                        w.enabledIf?.let { engine.eval(it, scopeBase) != 0 && engine.eval(it, scopeBase) != false }
                            ?: true
                    val stack = renderIcon(w.icon, scopeBase, player)
                    val button = if (enabled) w.onClick?.let { act ->
                        MenuButton(
                            cmd = act.run,
                            args = act.args?.let { JsonTemplater.templatizeStringMap(it, engine, scopeBase) }
                                ?: emptyMap())
                    } else null
                    out[w.slot] = if (button != null) stack.withButton(button) else stack
                }

                is WidgetDef.Label -> {
                    val stack = renderIcon(w.icon, scopeBase, player)
                    out[w.slot] = stack
                }

                is WidgetDef.ListWidget -> {
                    val listValue = nodeValues[w.items.from] as? List<*> ?: emptyList<Any?>()
                    val slots = w.layout.slots
                    for ((i, raw) in listValue.withIndex()) {
                        if (i >= slots.size) break
                        val valueMap = raw
                        val scope = scopeBase.copy(value = valueMap)
                        val stack = renderIcon(w.items.icon, scope, player)
                        val btn = w.items.onClick?.let { act ->
                            MenuButton(
                                cmd = engine.renderTemplate(act.run, scope),
                                args = act.args?.let { JsonTemplater.templatizeStringMap(it, engine, scope) }
                                    ?: emptyMap())
                        }
                        out[slots[i]] = if (btn != null) stack.withButton(btn) else stack
                    }
                }
            }
        }
        return RenderedMenu(out)
    }

    private fun ItemStack.withButton(btn: MenuButton): ItemStack {
        set(ModComponents.MENU_BUTTON, btn)
        return this
    }
}