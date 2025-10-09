package com.doofcraft.vessel.server.ui.expr

interface ExprEngine {
    /** Evaluation of simple expressions, returning a Primitive, Map or List. */
    fun eval(expr: String, scope: Scope): Any?

    /** Simple string templating with {field} & {? cond ? a : b } forms */
    fun renderTemplate(text: String, scope: Scope): String
}

data class Scope(
    val menu: Map<String, Any?> = emptyMap(),
    val params: Map<String, Any?> = emptyMap(),
    val player: Map<String, Any?> = emptyMap(),
    val nodeValues: Map<String, Any?> = emptyMap(),
    val value: Any? = null,
    val state: Map<String, Any?> = emptyMap()
)