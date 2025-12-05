package com.doofcraft.vessel.server.ui.render

import com.doofcraft.vessel.common.api.VesselIdentifier
import com.doofcraft.vessel.common.component.MenuButton
import com.doofcraft.vessel.common.registry.StackComponents
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.JsonTemplater
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.model.IconDef
import com.doofcraft.vessel.server.ui.model.MenuDefinition
import com.doofcraft.vessel.server.ui.model.WidgetDef
import com.doofcraft.vessel.server.ui.text.ComponentFactory
import com.doofcraft.vessel.server.util.isEmpty
import com.doofcraft.vessel.server.util.toText
import de.themoep.minedown.adventure.MineDown
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemLore
import kotlin.math.min

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
                val tokenRegex =
                    Regex("""((?:&[a-zA-Z0-9_#-]+&|##|\*\*|__|~~|\?\?)*)%([a-zA-Z0-9_]+)%((?:&[a-zA-Z_#-]+&|##|\*\*|__|~~|\?\?)*)""")
                for (line in lore) {
                    val m = tokenRegex.find(line)
                    val md = MineDown(line).replaceFirst(true)
                    if (m != null) {
                        val key = m.groupValues[2]
                        val multi = loreMultiLineRepls[key]
                        if (multi != null) {
                            if (line.trim() == "%$key%") {
                                // Whole line is the token -> replace entire line with many lines
                                finalLore += multi
                                continue
                            }

                            val beforeStyling = m.groupValues[1]
                            val afterStyling = m.groupValues[3]

                            // Extract just styling and token from the lore line.
                            val tokenOnly = MineDown("$beforeStyling%$key%$afterStyling").replaceFirst(true)

                            // Template line is more complex, replace line with first replacement
                            // then append rest, including any styling surrounding the token.
                            val first = multi.first()
                            finalLore += md.copy().apply { replace(key, first) }.toComponent()
                            val rest = multi.drop(1)
                            finalLore += rest.map { tokenOnly.copy().apply { replace(key, it) }.toComponent() }

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
                stack[DataComponents.LORE] = ItemLore(finalLore.filterNot { it.isEmpty() }.map { line ->
                    line.colorIfAbsent(NamedTextColor.GRAY).toText().copy().withStyle {
                        it.withItalic(false)
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
                    val scope =
                        w.value?.let { val value = engine.eval(it, scopeBase); scopeBase.copy(value = value) }
                            ?: scopeBase

                    val hidden = w.hideIf?.let {
                        val result = engine.eval(it, scope)
                        result != 0 && result != false
                    } ?: false
                    if (hidden) return@forEach

                    val enabled = w.enabledIf?.let {
                        val result = engine.eval(it, scope)
                        result != 0 && result != false
                    } ?: true

                    val stack = renderIcon(w.icon, scope, player)
                    val button = if (enabled) w.onClick?.let { act ->
                        MenuButton(cmd = act.run, args = act.args?.let { args ->
                            JsonTemplater.templatizeMap(args, engine, scope).mapValues { it.value ?: "" }
                        } ?: emptyMap())
                    } else null
                    out[w.slot] = if (button != null) stack.withButton(button) else stack
                }

                is WidgetDef.Label -> {
                    val scope =
                        w.value?.let { val value = engine.eval(it, scopeBase); scopeBase.copy(value = value) }
                            ?: scopeBase

                    val hidden = w.hideIf?.let {
                        val result = engine.eval(it, scope)
                        result != 0 && result != false
                    } ?: false
                    if (hidden) return@forEach

                    val stack = renderIcon(w.icon, scope, player)
                    out[w.slot] = stack
                }

                is WidgetDef.ListWidget -> {
                    val hidden = w.hideIf?.let {
                        val result = engine.eval(it, scopeBase)
                        result != 0 && result != false
                    } ?: false
                    if (hidden) return@forEach

                    val listSource = w.items.from?.let { engine.eval(it, scopeBase) } as? List<*>
                    val slots = w.layout.slots
                    // If there is a 'from' source, it.size, otherwise, slots.size
                    val size = listSource?.let { min(it.size, slots.size) } ?: slots.size
                    val listValue = listSource ?: emptyList<Any?>()
                    for (i in 0 until size) {
                        val raw = listValue.getOrNull(i) as? Map<*, *> ?: emptyMap<String, Any?>()
                        val scope = scopeBase.copy(value = raw + ("index" to i))
                        // Each individual element can also be hidden
                        val hidden = w.items.hideIf?.let {
                            val result = engine.eval(it, scope)
                            result != 0 && result != false
                        } ?: false
                        if (hidden) continue

                        val stack = renderIcon(w.items.icon, scope, player)
                        val enabled = w.items.enabledIf?.let {
                            val result = engine.eval(it, scope)
                            result != 0 && result != false
                        } ?: true

                        val btn = if (enabled) w.items.onClick?.let { act ->
                            MenuButton(cmd = engine.renderTemplate(act.run, scope), args = act.args?.let { args ->
                                JsonTemplater.templatizeMap(args, engine, scope).mapValues { it.value ?: "" }
                            } ?: emptyMap())
                        } else null
                        out[slots[i]] = if (btn != null) stack.withButton(btn) else stack
                    }
                }
            }
        }
        return RenderedMenu(out)
    }

    private fun ItemStack.withButton(btn: MenuButton): ItemStack {
        set(StackComponents.MENU_BUTTON, btn)
        return this
    }
}