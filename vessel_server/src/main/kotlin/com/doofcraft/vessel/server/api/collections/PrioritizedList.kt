package com.doofcraft.vessel.server.api.collections

/**
 * Functions like a list which is ordered by [Priority]. Within each category of priority,
 * order is first-come-first-served. Having the same value in multiple priorities is possible
 * but can cause stickiness.
 *
 * @author Hiroku
 * @since February 18th, 2022
 */
open class PrioritizedList<T> : Iterable<T> {
    protected val priorityMap = mutableMapOf<Priority, MutableList<T>>()
    // Please don't change arraylist, we need insertion order
    protected val ordered = arrayListOf<T>()

    val mapping: Map<Priority, List<T>>
        get() = priorityMap.toMap()

    private fun reorder() {
        ordered.clear()
        Priority.entries.forEach {
            priorityMap[it]?.let { ordered.addAll(it) }
        }
    }

    fun add(priority: Priority, value: T) {
        // Please don't change arraylist, we need insertion order
        priorityMap.putIfAbsent(priority, arrayListOf())
        priorityMap[priority]?.add(value)
        reorder()
    }

    fun remove(value: T) {
        priorityMap.values.forEach { it.remove(value) }
        reorder()
    }

    fun remove(priority: Priority, value: T) {
        priorityMap[priority]?.remove(value)
        reorder()
    }

    fun clear() {
        priorityMap.clear()
        ordered.clear()
    }

    fun isEmpty() = ordered.isEmpty()

    override fun iterator() = ordered.iterator()
}
