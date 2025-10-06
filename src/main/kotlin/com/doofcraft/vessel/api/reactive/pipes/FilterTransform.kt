package com.doofcraft.vessel.api.reactive.pipes

import com.doofcraft.vessel.api.reactive.Transform

/**
 * A transform that will only emit received values that match the given predicate.
 */
class FilterTransform<I>(private val predicate: (I) -> Boolean) : Transform<I, I> {
    override fun invoke(input: I): I {
        if (predicate(input)) {
            return input
        } else {
            noTransform(terminate = false)
        }
    }
}
