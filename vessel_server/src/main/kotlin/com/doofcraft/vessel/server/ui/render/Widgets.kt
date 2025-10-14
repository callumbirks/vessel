package com.doofcraft.vessel.server.ui.render

import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.common.registry.ModComponents
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.JsonTemplater
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.model.IconDef
import com.doofcraft.vessel.server.ui.model.MenuDefinition
import com.doofcraft.vessel.server.ui.model.WidgetDef
import com.doofcraft.vessel.server.ui.text.ComponentFactory
import com.doofcraft.vessel.server.util.toText
import de.themoep.minedown.adventure.MineDown
import net.kyori.adventure.text.Component
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.CommonColors
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore

data class RenderedMenu(
    val items: Map<Int, ItemStack>
)

class WidgetRenderer(
    private val engine: ExprEngine
) {
    fun renderAll(
        def: MenuDefinition,
        title: String,
        ctx: UiContext,
        player: ServerPlayer,
    ): RenderedMenu {
        val out = HashMap<Int, ItemStack>()
        val scopeBase = Scope(
            menu = mapOf("id" to def.id, "title" to title, "rows" to def.rows),
            params = ctx.params,
            player = mapOf("uuid" to player.uuid.toString(), "name" to player.scoreboardName),
            nodeValues = ctx.nodeValues,
            state = ctx.state,
        )

        fun renderIcon(icon: IconDef, scope: Scope, player: ServerPlayer): ItemStack {
            val itemIdStr = engine.renderTemplate(icon.item, scope)
            val itemId = VesselIdentifier.parse(itemIdStr)

            val name = icon.name?.let { engine.renderTemplate(it, scope) }
            val lore = icon.lore?.map { engine.renderTemplate(it, scope) } ?: emptyList()

            val builder = ItemBuilder.of(itemId).withName(name)

            val nameReplSpecs = icon.replacements?.name ?: emptyMap()
            val loreReplSpecs = icon.replacements?.lore ?: emptyMap()

            val loreSingleLineRepls = mutableMapOf<String, Component>()
            val loreMultiLineRepls = mutableMapOf<String, List<Component>>()

            for ((k, spec) in loreReplSpecs) {
                val lines = ComponentFactory.buildMultiLine(spec, engine, scope)
                if (lines.size <= 1) loreSingleLineRepls[k] = lines.firstOrNull() ?: Component.empty()
                else loreMultiLineRepls[k] = lines
            }

            val stack: ItemStack = if (loreMultiLineRepls.isEmpty()) {
                builder.withLore(lore).build(nameReplacements = {
                    nameReplSpecs.forEach { (placeholder, spec) ->
                        val comp = ComponentFactory.buildSingleLine(spec, engine, scope)
                        replace(placeholder, comp)
                    }
                }, loreReplacements = {
                    loreSingleLineRepls.forEach { (placeholder, replacement) ->
                        replace(placeholder, replacement)
                    }
                })
            } else {
                val finalLore = mutableListOf<Component>()
                val tokenRegex = Regex("%([a-zA-Z0-9_]+)%")
                for (line in lore) {
                    val m = tokenRegex.find(line)
                    val md = MineDown(line).replaceFirst(true)
                    if (m != null) {
                        val key = m.groupValues[1]
                        val multi = loreMultiLineRepls[key]
                        if (multi != null && line.trim() == "%$key%") {
                            // Whole line is the token -> replace entire line with many lines
                            finalLore += multi
                            continue
                        }
                        if (multi != null) {
                            // Token embedded in text -> duplicate the template line and replace each line
                            for (replacement in multi) {
                                val md = md.copy().apply {
                                    replace(key, replacement)
                                    loreSingleLineRepls.forEach { (k, v) -> if (k != key) replace(k, v) }
                                }
                                finalLore += md.toComponent()
                            }
                            continue
                        }
                        // Key is not a multiline replacement -> apply normal single-line replacements
                        val md = md.copy().apply { loreSingleLineRepls.forEach { (k, v) -> replace(k, v) } }
                        finalLore += md.toComponent()
                    } else {
                        finalLore += md.toComponent()
                    }
                }

                val stack = builder.build(nameReplacements = {
                    nameReplSpecs.forEach { (placeholder, spec) ->
                        val comp = ComponentFactory.buildSingleLine(spec, engine, scope)
                        replace(placeholder, comp)
                    }
                })
                stack[DataComponents.LORE] = ItemLore(finalLore.map { line ->
                    line.toText().copy().withStyle {
                        it.withColor(CommonColors.WHITE).withItalic(false)
                    }
                })
                stack
            }

            for (m in UiComponentMappers.all()) {
                if (!m.shouldApply(itemId, stack, scope, player)) continue
                m.apply(itemId, stack, scope, player)
            }

            return stack
        }

        def.widgets.forEach { w ->
            when (w) {
                is WidgetDef.Button -> {
                    val hidden = w.hideIf?.let {
                        val result = engine.eval(it, scopeBase)
                        result != 0 && result != false
                    } ?: false
                    if (hidden) return@forEach

                    val enabled = w.enabledIf?.let {
                        val result = engine.eval(it, scopeBase)
                        result != 0 && result != false
                    } ?: true

                    val stack = renderIcon(w.icon, scopeBase, player)
                    val button = if (enabled) w.onClick?.let { act ->
                        MenuButton(
                            cmd = act.run,
                            args = act.args?.let { JsonTemplater.templatizeMap(it, engine, scopeBase) } ?: emptyMap())
                    } else null
                    out[w.slot] = if (button != null) stack.withButton(button) else stack
                }

                is WidgetDef.Label -> {
                    val hidden = w.hideIf?.let {
                        val result = engine.eval(it, scopeBase)
                        result != 0 && result != false
                    } ?: false
                    if (hidden) return@forEach

                    val stack = renderIcon(w.icon, scopeBase, player)
                    out[w.slot] = stack
                }

                is WidgetDef.ListWidget -> {
                    val hidden = w.hideIf?.let {
                        val result = engine.eval(it, scopeBase)
                        result != 0 && result != false
                    } ?: false
                    if (hidden) return@forEach

                    val listValue = ctx.nodeValues[w.items.from] as? List<*> ?: emptyList<Any?>()
                    val slots = w.layout.slots
                    for ((i, raw) in listValue.filterIsInstance<Map<*, *>>().withIndex()) {
                        if (i >= slots.size) break
                        val scope = scopeBase.copy(value = raw + ("index" to i))
                        val stack = renderIcon(w.items.icon, scope, player)
                        val btn = w.items.onClick?.let { act ->
                            MenuButton(
                                cmd = engine.renderTemplate(act.run, scope),
                                args = act.args?.let { JsonTemplater.templatizeMap(it, engine, scope) } ?: emptyMap())
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