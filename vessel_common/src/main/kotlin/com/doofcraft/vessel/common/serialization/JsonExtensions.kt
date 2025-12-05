package com.doofcraft.vessel.common.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.serialization.json.*

fun kotlinx.serialization.json.JsonElement.toGsonElement(): JsonElement {
    return when (this) {
        is kotlinx.serialization.json.JsonArray -> {
            val array = JsonArray(this.size)
            for (elem in this) {
                array.add(elem.toGsonElement())
            }
            array
        }
        is kotlinx.serialization.json.JsonObject -> {
            val obj = JsonObject()
            for ((k, v) in this) {
                obj.add(k, v.toGsonElement())
            }
            obj
        }
        is kotlinx.serialization.json.JsonPrimitive -> {
            if (this.contentOrNull == null) JsonNull.INSTANCE
            else if (this.isString) JsonPrimitive(this.content)
            else {
                this.booleanOrNull?.let { return JsonPrimitive(it) }
                this.intOrNull?.let { return JsonPrimitive(it) }
                this.longOrNull?.let { return JsonPrimitive(it) }
                JsonPrimitive(this.double)
            }
        }
    }
}

fun JsonElement.toKxElement(): kotlinx.serialization.json.JsonElement {
    return when (this) {
        is JsonNull -> kotlinx.serialization.json.JsonNull
        is JsonPrimitive -> {
            val p = this.asJsonPrimitive
            when {
                p.isString -> kotlinx.serialization.json.JsonPrimitive(p.asString)
                p.isNumber -> kotlinx.serialization.json.JsonPrimitive(p.asNumber)
                p.isBoolean -> kotlinx.serialization.json.JsonPrimitive(p.asBoolean)
                else -> error { "Unknown JsonPrimitive type ${p.javaClass}" }
            }
        }
        is JsonArray -> {
            this.asJsonArray.asList().map { it.toKxElement() }.let { JsonArray(it) }
        }
        is JsonObject -> {
            this.asJsonObject.asMap().mapValues { (_, v) -> v.toKxElement() }.let { JsonObject(it) }
        }
        else -> error { "Unknown JsonElement type ${this.javaClass}"}
    }
}
