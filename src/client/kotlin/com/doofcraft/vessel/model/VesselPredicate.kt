package com.doofcraft.vessel.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

sealed interface VesselPredicate {
    fun test(stack: ItemStack): Boolean
    fun componentList(): List<ResourceLocation>

    data class ComponentOp(
        val componentId: ResourceLocation, val path: String?, val op: JsonOp
    ) : VesselPredicate {
        val component: DataComponentType<kotlin.Any>? by lazy {
            resolveComponent(componentId)
        }

        override fun test(stack: ItemStack): Boolean {
            if (component == null) return false
            val componentValue = stack.get(component!!) ?: return op is JsonOp.Exists && !op.value
            val json = toJson(component!!.codec()!!, componentValue) ?: return false
            val elem = jsonPointer(json, path)
            if (elem !is JsonPrimitive?) return false
            return op.test(elem)
        }

        override fun componentList(): List<ResourceLocation> {
            return listOf(this.componentId)
        }
    }

    data class All(val children: List<VesselPredicate>) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean = children.all { it.test(stack) }
        override fun componentList(): List<ResourceLocation> {
            return this.children.flatMap { it.componentList() }
        }
    }

    data class Any(val children: List<VesselPredicate>) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean = children.any { it.test(stack) }
        override fun componentList(): List<ResourceLocation> {
            return this.children.flatMap { it.componentList() }
        }
    }

    data class Not(val child: VesselPredicate) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean = !child.test(stack)
        override fun componentList(): List<ResourceLocation> {
            return this.child.componentList()
        }
    }

    sealed interface JsonOp {
        fun test(actual: JsonPrimitive?): Boolean

        data class Exists(val value: Boolean) : JsonOp {
            override fun test(actual: JsonPrimitive?) = (actual != null) == value
        }

        data class Eq(val expect: JsonPrimitive?) : JsonOp {
            override fun test(actual: JsonPrimitive?) = jsonEquals(actual, expect)
        }

        data class Ne(val expect: JsonPrimitive?) : JsonOp {
            override fun test(actual: JsonPrimitive?) = !jsonEquals(actual, expect)
        }

        data class In(val set: List<JsonPrimitive?>) : JsonOp {
            override fun test(actual: JsonPrimitive?): Boolean = set.any { jsonEquals(actual, it) }
        }
    }

    class Deserializer {
        @FromJson
        fun fromJson(reader: JsonReader): VesselPredicate {
            reader.beginObject()
            var component: ResourceLocation? = null
            var path: String? = null
            var op: JsonOp? = null
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "all" -> {
                        val list = parseList(reader)
                        reader.endObject()
                        return All(list)
                    }

                    "any" -> {
                        val list = parseList(reader)
                        reader.endObject()
                        return Any(list)
                    }

                    "not" -> {
                        val pred = fromJson(reader)
                        reader.endObject()
                        return Not(pred)
                    }

                    "component" -> component = ResourceLocation.parse(reader.nextString())
                    "path" -> path = reader.nextString()
                    "exists" -> op = JsonOp.Exists(reader.nextBoolean())
                    "eq" -> op = JsonOp.Eq(readPrimitive(reader))
                    "ne" -> JsonOp.Ne(readPrimitive(reader))
                    "in" -> {
                        val list = mutableListOf<JsonPrimitive?>()
                        reader.beginArray()
                        while (reader.hasNext()) list.add(readPrimitive(reader))
                        reader.endArray()
                        op = JsonOp.In(list)
                    }

                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (component == null) throw JsonDataException("'component' missing from predicate at ${reader.path}")
            if (op == null) throw JsonDataException("No op specified in predicate at ${reader.path}")
            return ComponentOp(component, path, op)
        }

        private fun parseList(reader: JsonReader): List<VesselPredicate> {
            val list = mutableListOf<VesselPredicate>()
            reader.beginArray()
            while (reader.hasNext()) {
                list.add(fromJson(reader))
            }
            reader.endArray()
            return list
        }

        private fun readPrimitive(reader: JsonReader): JsonPrimitive? {
            return when (reader.peek()) {
                Token.BOOLEAN -> JsonPrimitive(reader.nextBoolean())
                Token.NUMBER -> JsonPrimitive(reader.nextDouble())
                Token.STRING -> JsonPrimitive(reader.nextString())
                Token.NULL -> {
                    reader.skipValue()
                    return null
                }
                else -> throw JsonDataException("Expected primitive value at ${reader.path}")
            }
        }
    }
}

fun jsonEquals(a: JsonElement?, b: JsonElement?): Boolean {
    if (a === b) return true
    if (a == null || b == null) return false
    if (a.isJsonPrimitive && b.isJsonPrimitive) {
        val ap = a.asJsonPrimitive
        val bp = b.asJsonPrimitive
        if (ap.isNumber && bp.isNumber) return ap.asDouble == bp.asDouble
    }
    return a == b
}

@Suppress("UNCHECKED_CAST")
fun resolveComponent(id: ResourceLocation): DataComponentType<Any>? {
    return BuiltInRegistries.DATA_COMPONENT_TYPE.get(id) as? DataComponentType<Any>
}

fun <T> toJson(codec: Codec<T>, value: T): JsonElement? {
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