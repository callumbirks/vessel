package com.doofcraft.vessel.ui.render

import com.doofcraft.vessel.ui.expr.ExprEngine
import com.doofcraft.vessel.ui.expr.Scope
import com.doofcraft.vessel.ui.model.IconDef
import com.doofcraft.vessel.ui.model.MenuDefinition
import com.doofcraft.vessel.ui.model.WidgetDef
import com.doofcraft.vessel.ui.text.ComponentFactory
import com.doofcraft.vessel.api.VesselIdentifier
import com.doofcraft.vessel.component.MenuButton
import com.doofcraft.vessel.registry.ModComponents
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
        nodeValues: Map<String, Any?>,
        state: Map<String, Any?>,
        player: ServerPlayer,
    ): RenderedMenu {
        val out = HashMap<Int, ItemStack>()
        val scopeBase = Scope(
            menu = mapOf("id" to def.id, "title" to def.title, "rows" to def.rows),
            player = mapOf("uuid" to player.uuid.toString(), "name" to player.scoreboardName),
            nodeValues = nodeValues,
            state = state,
        )

        fun renderIcon(icon: IconDef, scope: Scope): ItemStack {
            val itemId = engine.renderTemplate(icon.item, scope)

            val name = icon.name?.let { engine.renderTemplate(it, scope) }
            val lore = icon.lore?.map { engine.renderTemplate(it, scope) } ?: emptyList()

            val builder = ItemBuilder.of(VesselIdentifier.parse(itemId)).withName(name).withLore(lore)

            val nameRepl = icon.replacements?.name ?: emptyMap()
            val loreRepl = icon.replacements?.lore ?: emptyMap()

            val result: ItemStack = builder.build(nameReplacements = {
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
            return result
        }

        def.widgets.forEach { w ->
            when (w) {
                is WidgetDef.Button -> {
                    val enabled =
                        w.enabledIf?.let { engine.eval(it, scopeBase) != 0 && engine.eval(it, scopeBase) != false } ?: true
                    val stack = renderIcon(w.icon, scopeBase)
                    val button = if (enabled) w.onClick?.let { act ->
                        MenuButton(
                            MenuButton.Action.ACCEPT,
                            data = mapOf("cmd" to act.run) + (act.args?.let { mapOf("args" to it.toString()) }
                                ?: emptyMap()))
                    } else null
                    out[w.slot] = if (button != null) stack.withButton(button) else stack
                }
                is WidgetDef.Label -> {
                    val stack = renderIcon(w.icon, scopeBase)
                    out[w.slot] = stack
                }
                is WidgetDef.ListWidget -> {
                    val listValue = nodeValues[w.items.from] as? List<*> ?: emptyList<Any?>()
                    val slots = w.layout.slots
                    for ((i, raw) in listValue.withIndex()) {
                        if (i >= slots.size) break
                        val valueMap = raw
                        val scope = scopeBase.copy(value = valueMap)
                        val stack = renderIcon(w.items.icon, scope)
                        val btn = w.items.onClick?.let { act ->
                            MenuButton(
                                MenuButton.Action.ACCEPT,
                                mapOf(
                                    "cmd" to engine.renderTemplate(act.run, scope),
                                    "args" to (act.args?.let { engine.renderTemplate(it.toString(), scope) } ?: "")
                                )
                            )
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