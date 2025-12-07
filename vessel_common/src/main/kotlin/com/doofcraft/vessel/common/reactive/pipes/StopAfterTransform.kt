package com.doofcraft.vessel.common.reactive.pipes

import com.doofcraft.vessel.common.reactive.Transform

/**
 * A transform that will emit values until a condition is met, except that when the
 * condition is met, it will emit on that final case.
 *
 * @author Hiroku
 * @since May 1st, 2022
 */
class StopAfterTransform<I>(val predicate: (I) -> Boolean) : Transform<I, I> {
    var finished = false
    override fun invoke(input: I): I {
        if (finished) {
            noTransform(true)
        }
        if (predicate(input)) {
            finished = true
        }
        return input
    }
}
