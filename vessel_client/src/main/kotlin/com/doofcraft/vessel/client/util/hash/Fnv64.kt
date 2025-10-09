package com.doofcraft.vessel.client.util.hash

import kotlinx.serialization.json.*
import java.lang.Double
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.code
import kotlin.repeat

class Fnv64 {
    private var h = -0x340d631b7bdddcdbL
    fun updateLong(x: Long) {
        var v = x; repeat(8) { h = (h xor (v and 0xff)).times(0x100000001b3L); v = v ushr 8 }
    }

    fun updateStr(s: String) {
        for (ch in s) {
            h = (h xor (ch.code.toLong() and 0xff)).times(0x100000001b3L)
        }
    }

    fun updateBool(b: Boolean) = updateLong(if (b) 1L else 0L)
    fun updateJson(e: JsonElement?) {
        when (e) {
            null -> updateLong(0)
            is JsonNull -> updateLong(1)
            is JsonPrimitive -> {
                when {
                    e.isString -> updateStr(e.toString())
                    e.booleanOrNull != null -> updateBool(e.boolean)
                    else -> updateLong(Double.doubleToRawLongBits(e.double))
                }
            }

            is JsonArray -> {
                updateLong(5); e.jsonArray.forEach { updateJson(it) }
            }

            else -> {
                updateLong(7)
                val obj = e.jsonObject
                obj.keys.toMutableList().sorted().forEach { k -> updateStr(k); updateJson(obj[k]) }
            }
        }
    }

    fun value() = h
}