package com.doofcraft.vessel.server.api.reactive.pipes

import com.doofcraft.vessel.server.api.reactive.Transform

/**
 * A transform which will only take some number of emissions before terminating the observable subscription.
 */
class TakeFirstTransform<I>(private var amount: Int = 1) : Transform<I, I> {
    override fun invoke(input: I): I {
        if (amount > 0) {
            amount--
            return input
        } else {
            noTransform(terminate = true)
        }
    }
}
