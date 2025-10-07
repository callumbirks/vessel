package com.doofcraft.vessel.server.api.reactive.pipes

import com.doofcraft.vessel.server.api.reactive.Transform

/**
 * A transform that will ignore some number of initial emissions before it will continue as usual.
 */
class IgnoreFirstTransform<T>(var amount: Int = 1) : Transform<T, T> {
    override fun invoke(input: T): T {
        if (amount > 0) {
            amount--
            noTransform(terminate = false)
        } else {
            return input
        }
    }
}
