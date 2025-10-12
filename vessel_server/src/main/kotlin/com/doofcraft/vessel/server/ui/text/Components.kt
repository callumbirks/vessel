package com.doofcraft.vessel.server.ui.text

import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.Scope
import com.doofcraft.vessel.server.ui.model.ComponentSpec
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
                        is ComponentSpec.Translatable.Arg.FromNode -> {
                            val value = engine.eval(arg.path, scope)
                            return value as? AdvComponent ?: AdvComponent.empty()
                        }
                    }
                }
                AdvComponent.translatable(key, args)
            }
            is ComponentSpec.FromNode -> {
                val value = engine.eval(spec.path, scope)
                return value as? AdvComponent ?: AdvComponent.empty()
            }
        }
    }
}