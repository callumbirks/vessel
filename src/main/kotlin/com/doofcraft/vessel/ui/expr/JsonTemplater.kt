package com.doofcraft.vessel.ui.expr

import kotlinx.serialization.json.*

object JsonTemplater {
    fun templatize(el: JsonElement, engine: ExprEngine, scope: Scope): Any? {
        return when (el) {
            is JsonObject -> el.mapValues { (_, v) -> templatize(v, engine, scope) }
            is JsonArray -> el.map { templatize(it, engine, scope) }
            is JsonPrimitive -> if (el.isString) engine.renderTemplate(el.content, scope) else el.booleanOrNull
                ?: el.longOrNull ?: el.doubleOrNull ?: el.content
        }
    }
}