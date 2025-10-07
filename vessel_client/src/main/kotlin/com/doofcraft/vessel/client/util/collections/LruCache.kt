package com.doofcraft.vessel.client.util.collections

class LruCache<K, V>(private val maxSize: Int): LinkedHashMap<K, V>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K?, V?>?) = size > maxSize
}