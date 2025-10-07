package com.doofcraft.vessel.server.api.reactive.pipes

import com.doofcraft.vessel.server.api.reactive.Transform

/**
 * A transform that will continue emitting values for as long as the given predicate is met.
 *
 * This will only unsubscribe the stream once a value is emitted and the predicate is false.
 */
class EmitWhileTransform<I>(private val predicate: (I) -> Boolean) : Transform<I, I> {
    override fun invoke(input: I): I {
        if (predicate(input)) {
            return input
        } else {
            noTransform(terminate = true)
        }
    }
}
