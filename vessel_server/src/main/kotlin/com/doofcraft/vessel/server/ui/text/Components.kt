package com.doofcraft.vessel.server.ui.text

import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.model.ComponentSpec
import kotlin.collections.get
import net.kyori.adventure.text.Component as AdvComponent

object ComponentFactory {
    fun build(spec: ComponentSpec, engine: ExprEngine, scope: Scope): AdvComponent {
        return when (spec) {
            is ComponentSpec.Literal -> {
                val txt = engine.renderTemplate(spec.text, scope)
                AdvComponent.text(txt)
            }
            is ComponentSpec.Translatable -> {
                val key = engine.renderTemplate(spec.key, scope)
                val args: List<AdvComponent> = spec.args.map { arg ->
                    when (arg) {
                        is ComponentSpec.Translatable.Arg.Literal ->
                            AdvComponent.text(engine.renderTemplate(arg.text, scope))
                        is ComponentSpec.Translatable.Arg.FromNode ->
                            lookupNodeComponent(arg.path, scope) ?: AdvComponent.empty()
                    }
                }
                AdvComponent.translatable(key, args)
            }
            is ComponentSpec.FromNode -> {
                lookupNodeComponent(spec.path, scope) ?: AdvComponent.empty()
            }
        }
    }

    /** Fetch a Kyori Component previously produced by data nodes */
    private fun lookupNodeComponent(path: String, scope: Scope): AdvComponent? {
        val parts = path.split('.')
        val root = scope.nodeValues[parts.first()] ?: return null
        var cur: Any? = root
        for (i in 1 until parts.size) {
            cur = when (cur) {
                is Map<*, *> -> cur[parts[i]]
                else -> return null
            }
        }
        return when (cur) {
            is AdvComponent -> cur
            is String -> AdvComponent.text(cur)
            else -> null
        }
    }
}