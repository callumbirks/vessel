package com.doofcraft.vessel.model

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.minecraft.component.ComponentType
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.lang.reflect.Type

sealed interface VesselPredicate {
    fun test(stack: ItemStack): Boolean

    data class ComponentOp(
        val componentId: Identifier, val path: String?, val op: JsonOp
    ) : VesselPredicate {
        val component: ComponentType<kotlin.Any>? by lazy {
            resolveComponent(componentId)
        }

        override fun test(stack: ItemStack): Boolean {
            if (component == null) return false
            val any = stack.get(component) ?: return op is JsonOp.Exists && !op.value
            val json = toJson(component!!.codec!!, any) ?: return false
            val elem = jsonPointer(json, path)
            return op.test(elem)
        }
    }

    data class All(val children: List<VesselPredicate>) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean = children.all { it.test(stack) }
    }

    data class Any(val children: List<VesselPredicate>) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean = children.any { it.test(stack) }
    }

    data class Not(val child: VesselPredicate) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean = !child.test(stack)
    }

    sealed interface JsonOp {
        fun test(actual: JsonElement?): Boolean

        data class Exists(val value: Boolean) : JsonOp {
            override fun test(actual: JsonElement?) = (actual != null) == value
        }

        data class Eq(val expect: JsonElement) : JsonOp {
            override fun test(actual: JsonElement?) = jsonEquals(actual, expect)
        }

        data class Ne(val expect: JsonElement) : JsonOp {
            override fun test(actual: JsonElement?) = !jsonEquals(actual, expect)
        }

        data class In(val set: List<JsonElement>) : JsonOp {
            override fun test(actual: JsonElement?): Boolean = set.any { jsonEquals(actual, it) }
        }

        data class Contains(val needle: JsonElement) : JsonOp {
            override fun test(actual: JsonElement?): Boolean = when {
                actual == null -> false
                actual.isJsonArray -> actual.asJsonArray.any { jsonEquals(it, needle) }
                actual.isJsonPrimitive && actual.asJsonPrimitive.isString && needle.isJsonPrimitive && needle.asJsonPrimitive.isString -> actual.asString.contains(
                    needle.asString
                )

                else -> false
            }
        }

        data class Range(val min: Double?, val max: Double?, val inclusive: Boolean = true) : JsonOp {
            override fun test(actual: JsonElement?): Boolean {
                val x = actual?.asJsonPrimitive?.asDoubleOrNull() ?: return false
                val lo = min?.let { if (inclusive) x >= it else x > it } ?: true
                val hi = max?.let { if (inclusive) x <= it else x < it } ?: true
                return lo && hi
            }
        }
    }

    class Deserializer : JsonDeserializer<VesselPredicate> {
        override fun deserialize(
            json: JsonElement, typeOfT: Type, context: JsonDeserializationContext
        ): VesselPredicate {
            val obj = json.asJsonObject
            when {
                obj.has("all") -> return All(obj.getAsJsonArray("all").map { deserialize(it, typeOfT, context) })
                obj.has("any") -> return Any(obj.getAsJsonArray("any").map { deserialize(it, typeOfT, context) })
                obj.has("not") -> return Not(deserialize(obj.get("not"), typeOfT, context))
            }

            val component = Identifier.of(obj.getAsJsonPrimitive("component").asString)
            val path = obj.getAsJsonPrimitive("path")?.asString

            val op: JsonOp = when {
                obj.has("exists") -> JsonOp.Exists(obj.getAsJsonPrimitive("exists").asBoolean)
                obj.has("eq") -> JsonOp.Eq(obj.get("eq"))
                obj.has("ne") -> JsonOp.Ne(obj.get("ne"))
                obj.has("in") -> JsonOp.In(obj.getAsJsonArray("in").toList())
                obj.has("contains") -> JsonOp.Contains(obj.get("contains"))
                obj.has("range") -> obj.getAsJsonObject("range").let { r ->
                    JsonOp.Range(r["min"]?.asDouble, r["max"]?.asDouble, r["inclusive"]?.asBoolean ?: true)
                }
                else -> throw JsonParseException("No operator for component $component in $obj")
            }
            return ComponentOp(component, path, op)
        }
    }
}

private fun jsonEquals(a: JsonElement?, b: JsonElement?): Boolean {
    if (a === b) return true
    if (a == null || b == null) return false
    if (a.isJsonPrimitive && b.isJsonPrimitive) {
        val ap = a.asJsonPrimitive
        val bp = b.asJsonPrimitive
        if (ap.isNumber && bp.isNumber) return ap.asDouble == bp.asDouble
    }
    return a == b
}

private fun JsonPrimitive.asDoubleOrNull(): Double? = if (isNumber) asNumber.toDouble() else asString.toDoubleOrNull()

private fun JsonArray.toList(): List<JsonElement> = map { it.deepCopy() }

@Suppress("UNCHECKED_CAST")
private fun resolveComponent(id: Identifier): ComponentType<Any>? {
    return Registries.DATA_COMPONENT_TYPE.get(id) as? ComponentType<Any>
}

private fun <T> toJson(codec: Codec<T>, value: T): JsonElement? {
    val result = codec.encodeStart(JsonOps.INSTANCE, value)
    return result.result().orElse(null)
}

private fun jsonPointer(root: JsonElement?, pointer: String?): JsonElement? {
    if (root == null || pointer == null || pointer.isEmpty() || pointer == "/") return root
    var cur: JsonElement? = root
    pointer.trim().removePrefix("/").split('/').forEach { raw ->
        val key = raw.replace("~1", "/").replace("~0", "~")
        cur = when (val c = cur) {
            is JsonObject -> if (c.has(key)) c.get(key) else return null
            is JsonArray -> key.toIntOrNull()?.let { idx -> c.get(idx) } ?: return null
            else -> return null
        }
    }
    return cur
}