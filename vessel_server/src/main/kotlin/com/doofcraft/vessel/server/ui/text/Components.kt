package com.doofcraft.vessel.server.ui.text

import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.model.ComponentSpec
import net.kyori.adventure.text.Component as AdvComponent

object ComponentFactory {
    fun buildSingleLine(spec: ComponentSpec, engine: ExprEngine, scope: Scope): AdvComponent {
        return buildMultiLine(spec, engine, scope).firstOrNull() ?: AdvComponent.empty()
    }

    fun buildMultiLine(spec: ComponentSpec, engine: ExprEngine, scope: Scope): List<AdvComponent> {
        return when (spec) {
            is ComponentSpec.Literal -> {
                val txt = engine.renderTemplate(spec.text, scope)
                txt.lines().map { AdvComponent.text(it) }
            }
            is ComponentSpec.Translatable -> {
                val key = engine.renderTemplate(spec.key, scope)
                val args: List<AdvComponent> = spec.args.map { arg ->
                    when (arg) {
                        is ComponentSpec.Translatable.Arg.Literal ->
                            AdvComponent.text(engine.renderTemplate(arg.text, scope))
                        is ComponentSpec.Translatable.Arg.FromNode -> {
                            val value = engine.eval(arg.path, scope)
                            value as? AdvComponent ?: AdvComponent.empty()
                        }
                    }
                }
                listOf(AdvComponent.translatable(key, args))
            }
            is ComponentSpec.FromNode -> {
                val value = engine.eval(spec.path, scope)
                when (value) {
                    is AdvComponent -> listOf(value)
                    is List<*> -> {
                        if (value.isEmpty()) listOf(AdvComponent.empty())
                        else if (value.first() is AdvComponent) value as List<AdvComponent>
                        else if (value.first() is String) (value as List<String>).map { AdvComponent.text(it) }
                        else listOf(AdvComponent.empty())
                    }
                    is String -> value.lines().map { AdvComponent.text(it) }
                    else -> listOf(AdvComponent.empty())
                }
            }
        }
    }
}