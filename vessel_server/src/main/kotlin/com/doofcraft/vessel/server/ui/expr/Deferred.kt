package com.doofcraft.vessel.server.ui.expr

sealed interface Deferred

data class ExprRef(val expr: String) : Deferred
data class TemplateRef(val tpl: String) : Deferred

fun Any?.evalDeferred(engine: ExprEngine, scope: Scope): Any? = when (this) {
    is ExprRef -> engine.eval(expr, scope)
    is TemplateRef -> engine.renderTemplate(tpl, scope)
    else -> this
}