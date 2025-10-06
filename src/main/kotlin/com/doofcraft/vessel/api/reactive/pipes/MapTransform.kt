package com.doofcraft.vessel.api.reactive.pipes

import com.doofcraft.vessel.api.reactive.Transform

/**
 * A transform that transforms the emitted values from one value to another using the given mapping function.
 */
class MapTransform<I, O>(private val mapping: (I) -> O) : Transform<I, O> {
    override fun invoke(input: I): O {
        return mapping(input)
    }
}
