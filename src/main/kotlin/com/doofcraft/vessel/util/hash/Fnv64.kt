package com.doofcraft.vessel.util.hash

import com.google.gson.JsonElement

class Fnv64 {
    private var h = -0x340d631b7bdddcdbL
    fun updateLong(x: Long) {
        var v = x; repeat(8) { h = (h xor (v and 0xff)).times(0x100000001b3L); v = v ushr 8 }
    }
    fun updateStr(s: String) {
        for (ch in s) { h = (h xor (ch.code.toLong() and 0xff)).times(0x100000001b3L) }
    }
    fun updateBool(b: Boolean) = updateLong(if (b) 1L else 0L)
    fun updateJson(e: JsonElement?) {
        when {
            e == null -> updateLong(0)
            e.isJsonNull -> updateLong(1)
            e.isJsonPrimitive -> {
                val p = e.asJsonPrimitive
                when {
                    p.isBoolean -> updateBool(p.asBoolean)
                    p.isNumber -> updateLong(java.lang.Double.doubleToRawLongBits(p.asDouble))
                    else -> updateStr(p.asString)
                }
            }
            e.isJsonArray -> { updateLong(5); e.asJsonArray.forEach { updateJson(it) }}
            else -> {
                updateLong(7)
                val obj = e.asJsonObject
                obj.keySet().toMutableList().sorted().forEach { k -> updateStr(k); updateJson(obj.get(k)) }
            }
        }
    }
    fun value() = h
}