@file:UseSerializers(ResourceLocationSerializer::class)

package com.doofcraft.vessel.common.predicate

import com.doofcraft.vessel.common.serialization.VesselJSON
import com.doofcraft.vessel.common.serialization.adapters.ItemHolderSerializer
import com.doofcraft.vessel.common.serialization.adapters.ItemStackSerializer
import com.doofcraft.vessel.common.serialization.adapters.ResourceLocationSerializer
import com.doofcraft.vessel.common.serialization.toGsonElement
import com.doofcraft.vessel.common.serialization.toKxElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

@Serializable(with = VesselPredicateSerializer::class)
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

    data class MatchStack(
        val stack: ItemStack
    ) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean {
            return ItemStack.matches(this.stack, stack)
        }

        override fun componentList(): List<ResourceLocation> = emptyList()
    }

    data class MatchItem(
        val item: Holder<Item>
    ) : VesselPredicate {
        override fun test(stack: ItemStack): Boolean {
            return stack.itemHolder == item
        }

        override fun componentList(): List<ResourceLocation> = emptyList()
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
            override fun test(actual: JsonPrimitive?) = actual == expect
        }

        data class Ne(val expect: JsonPrimitive?) : JsonOp {
            override fun test(actual: JsonPrimitive?) = actual == expect
        }

        data class In(val set: List<JsonPrimitive?>) : JsonOp {
            override fun test(actual: JsonPrimitive?): Boolean = set.any { actual == it }
        }
    }

    companion object {
        val CODEC: Codec<VesselPredicate> = ExtraCodecs.JSON.xmap({
            VesselJSON.JSON.decodeFromJsonElement(
                VesselPredicateSerializer, it.toKxElement()
            )
        }, { VesselJSON.JSON.encodeToJsonElement(VesselPredicateSerializer, it).toGsonElement() })

        val EMPTY: VesselPredicate = All(emptyList())
    }
}

object VesselPredicateSerializer : KSerializer<VesselPredicate> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.doofcraft.vessel.VesselPredicate")

    override fun deserialize(decoder: Decoder): VesselPredicate {
        val jd = decoder as? JsonDecoder ?: error("VesselPredicateSerializer supports JSON only")
        val elem = jd.decodeJsonElement()
        val json = jd.json
        require(elem is JsonObject) { "Expected object" }

        when {
            "all" in elem -> {
                val arr = elem.getValue("all")
                require(arr is JsonArray) { "'all' must be an array" }
                val children = arr.map { json.decodeFromJsonElement(this, it) }
                return VesselPredicate.All(children)
            }

            "any" in elem -> {
                val arr = elem.getValue("any")
                require(arr is JsonArray) { "'any' must be an array" }
                val children = arr.map { json.decodeFromJsonElement(this, it) }
                return VesselPredicate.Any(children)
            }

            "not" in elem -> {
                val child = json.decodeFromJsonElement(this, elem.getValue("not"))
                return VesselPredicate.Not(child)
            }

            "component" in elem -> {
                val comp = elem.getValue("component")
                val compId = json.decodeFromJsonElement(ResourceLocationSerializer, comp)
                val path = (elem["path"] as? JsonPrimitive)?.contentOrNull
                val op = decodeJsonOp(obj = elem, json = json)
                return VesselPredicate.ComponentOp(compId, path, op)
            }

            "match_stack" in elem -> {
                val stackJson = elem.getValue("match_stack")
                val stack = json.decodeFromJsonElement(ItemStackSerializer, stackJson)
                return VesselPredicate.MatchStack(stack)
            }

            "match_item" in elem -> {
                val itemJson = elem.getValue("match_item")
                val item = json.decodeFromJsonElement(ItemHolderSerializer, itemJson)
                return VesselPredicate.MatchItem(item)
            }

            else -> error("Could not determine predicate variant from keys: ${elem.keys}")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: VesselPredicate) {
        if (encoder !is JsonEncoder) error { "VesselPredicateSerializer supports JSON only" }
        val json = encoder.json

        val obj: JsonObject = when (value) {
            is VesselPredicate.All -> JsonObject(
                mapOf(
                    "all" to JsonArray(
                        value.children.map { child ->
                            json.encodeToJsonElement(this, child)
                        }
                    )
                )
            )
            is VesselPredicate.Any -> JsonObject(
                mapOf(
                    "any" to JsonArray(
                        value.children.map { child ->
                            json.encodeToJsonElement(this, child)
                        }
                    )
                )
            )
            is VesselPredicate.Not -> JsonObject(
                mapOf(
                    "not" to json.encodeToJsonElement(this, value.child)
                )
            )
            is VesselPredicate.ComponentOp -> {
                val base = mutableMapOf<String, JsonElement>(
                    "component" to json.encodeToJsonElement(ResourceLocationSerializer, value.componentId)
                )

                value.path?.let { path ->
                    base["path"] = JsonPrimitive(path)
                }

                base += encodeJsonOp(value.op)
                JsonObject(base)
            }
            is VesselPredicate.MatchStack -> JsonObject(
                mapOf(
                    "match_stack" to json.encodeToJsonElement(ItemStackSerializer, value.stack)
                )
            )
            is VesselPredicate.MatchItem -> JsonObject(
                mapOf(
                    "match_item" to json.encodeToJsonElement(ItemHolderSerializer, value.item)
                )
            )
        }

        encoder.encodeJsonElement(obj)
    }

    /* --- helpers to delegate JsonOp inside the same JSON object ("structural" sum) --- */

    fun decodeJsonOp(obj: JsonObject, json: Json): VesselPredicate.JsonOp {
        return when {
            "exists" in obj -> VesselPredicate.JsonOp.Exists(
                (obj.getValue("exists") as? JsonPrimitive)?.booleanOrNull ?: error("'exists' must be boolean")
            )

            "eq" in obj -> VesselPredicate.JsonOp.Eq(
                (obj["eq"])?.let { it as? JsonPrimitive })

            "ne" in obj -> VesselPredicate.JsonOp.Ne(
                (obj["ne"])?.let { it as? JsonPrimitive })

            "in" in obj -> {
                val arr = obj.getValue("in") as? JsonArray ?: error("'in' must be array")
                val list = arr.map { (it as? JsonPrimitive) }
                VesselPredicate.JsonOp.In(list)
            }

            else -> error("No op specified; expected one of exists/eq/ne/in")
        }
    }

    fun encodeJsonOp(op: VesselPredicate.JsonOp): Map<String, JsonElement> = when (op) {
        is VesselPredicate.JsonOp.Exists -> mapOf("exists" to JsonPrimitive(op.value))
        is VesselPredicate.JsonOp.Eq -> mapOf("eq" to op.expect!!)
        is VesselPredicate.JsonOp.Ne -> mapOf("ne" to op.expect!!)
        is VesselPredicate.JsonOp.In -> mapOf(
            "in" to JsonArray(op.set.map { it!! })
        )
    }
}

object JsonOpSerializer : KSerializer<VesselPredicate.JsonOp> {
    // This serializer is used only if JsonOp appears standalone (rare).
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("JsonOp")
    override fun deserialize(decoder: Decoder): VesselPredicate.JsonOp {
        val jd = decoder as? JsonDecoder ?: error("JsonOpSerializer supports JSON only")
        val obj = jd.decodeJsonElement()
        require(obj is JsonObject) { "JsonOp must be an object" }
        return VesselPredicateSerializer.run {
            decodeJsonOp(obj, jd.json)
        }
    }

    override fun serialize(encoder: Encoder, value: VesselPredicate.JsonOp) {
        val je = encoder as? JsonEncoder ?: error("JsonOpSerializer supports JSON only")
        je.encodeJsonElement(JsonObject(VesselPredicateSerializer.encodeJsonOp(value)))
    }
}

@Suppress("UNCHECKED_CAST")
fun resolveComponent(id: ResourceLocation): DataComponentType<Any>? {
    return BuiltInRegistries.DATA_COMPONENT_TYPE.get(id) as? DataComponentType<Any>
}

fun <T> toJson(codec: Codec<T>, value: T): JsonElement? {
    val result = codec.encodeStart(JsonOps.INSTANCE, value)
    val gson = result.result().orElse(null) ?: return null
    return gson.toKxElement()
}

private fun jsonPointer(root: JsonElement?, pointer: String?): JsonElement? {
    if (root == null || pointer == null || pointer.isEmpty() || pointer == "/") return root
    var cur: JsonElement? = root
    pointer.trim().removePrefix("/").split('/').forEach { raw ->
        val key = raw.replace("~1", "/").replace("~0", "~")
        cur = when (val c = cur) {
            is JsonObject -> if (c.containsKey(key)) c[key] else return null
            is JsonArray -> key.toIntOrNull()?.let { idx -> c[idx] } ?: return null
            else -> return null
        }
    }
    return cur
}