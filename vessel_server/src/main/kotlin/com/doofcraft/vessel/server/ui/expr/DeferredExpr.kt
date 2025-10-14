package com.doofcraft.vessel.server.ui.expr

sealed interface DeferredExpr {
    fun eval(engine: ExprEngine, scope: Scope): Any?
}

data class ExprRef(val expr: String) : DeferredExpr {
    override fun eval(engine: ExprEngine, scope: Scope): Any? {
        return engine.eval(expr, scope)
    }
}
data class TemplateRef(val tpl: String) : DeferredExpr {
    override fun eval(engine: ExprEngine, scope: Scope): Any? {
        return engine.renderTemplate(tpl, scope)
    }
}

fun Any?.evalDeferred(engine: ExprEngine, scope: Scope): Any? {
    return when (this) {
        is DeferredExpr -> this.eval(engine, scope)
        else -> this
    }
}