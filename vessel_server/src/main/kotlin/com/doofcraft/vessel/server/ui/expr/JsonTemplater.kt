package com.doofcraft.vessel.server.ui.expr

import kotlinx.serialization.json.*

object JsonTemplater {
    private val singleHole = Regex("""^\s*\{\s*([^?].*?)\s*}\s*$""", RegexOption.DOT_MATCHES_ALL)

    fun templatize(el: JsonElement, engine: ExprEngine, scope: Scope): Any? {
        return when (el) {
            is JsonObject -> {
                el[$$"$expr"]?.let { ExprRef(it.jsonPrimitive.content) }
                    ?: el[$$"$template"]?.let { TemplateRef(it.jsonPrimitive.content) }
                    ?: el.mapValues { (_, v) -> templatize(v, engine, scope) }
            }
            is JsonArray -> el.map { templatize(it, engine, scope) }
            is JsonPrimitive -> if (el.isString) templatizeString(el.content, engine, scope) else el.booleanOrNull
                ?: el.longOrNull ?: el.doubleOrNull ?: el.content
        }
    }

    fun templatizeString(el: String, engine: ExprEngine, scope: Scope): Any? {
        val m = singleHole.matchEntire(el)
        if (m != null) {
            // If the whole string is just one { ... } expression, evaluate and return the raw value.
            val expr = m.groupValues[1]
            return engine.eval(expr, scope)
        } else {
            // Otherwise, treat it as a template and return a rendered string.
            return engine.renderTemplate(el, scope)
        }
    }

    fun templatizeMapValues(map: Map<String, JsonElement>, engine: ExprEngine, scope: Scope): Map<String, Any?> {
        return map.mapValues { (_, v) -> templatize(v, engine, scope) }
    }

    fun templatizeStringMap(map: Map<String, String>, engine: ExprEngine, scope: Scope): Map<String, String> {
        // TODO: Support 'templatizeString()' by making MenuButton args support more than String values, maybe via JSON?
        val result = map.mapKeys { (k, _) -> engine.renderTemplate(k, scope) }
            .mapValues { (_, v) -> engine.renderTemplate(v, scope) }
        return result
    }
}