package com.doofcraft.vessel.server.ui.text

import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.JsonTemplater
import com.doofcraft.vessel.server.ui.expr.Scope
import net.kyori.adventure.text.Component as AdvComponent

object ComponentFactory {
    fun buildSingleLine(expr: String, engine: ExprEngine, scope: Scope): AdvComponent {
        return buildMultiLine(expr, engine, scope).firstOrNull() ?: AdvComponent.empty()
    }

    fun buildMultiLine(expr: String, engine: ExprEngine, scope: Scope): List<AdvComponent> {
        val value = JsonTemplater.templatizeString(expr, engine, scope)
        return when (value) {
            is AdvComponent -> listOf(value)
            is List<*> -> {
                if (value.isEmpty()) listOf(AdvComponent.empty())
                else if (value.first() is AdvComponent) value as List<AdvComponent>
                else if (value.first() is String) (value as List<String>).map { AdvComponent.text(it) }
                else listOf(AdvComponent.empty())
            }
            is String -> value.lines().map { AdvComponent.text(it) }
            else -> listOf(AdvComponent.text(value.toString()))
        }
    }
}