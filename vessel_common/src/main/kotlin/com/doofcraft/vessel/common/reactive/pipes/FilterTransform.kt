package com.doofcraft.vessel.common.reactive.pipes

import com.doofcraft.vessel.common.reactive.Transform

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
