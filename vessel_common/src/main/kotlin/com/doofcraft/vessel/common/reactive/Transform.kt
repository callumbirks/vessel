package com.doofcraft.vessel.common.reactive
/**
 * A transformation function that can be used in [Observable.pipe] to translate the [Observable] in some way.
 * If an input value should not have an output, using [Transform.noTransform] will throw a controlled exception
 * which prevents the [Observable] from propagating anything for that input. That function has a parameter for
 * whether the transformed [Observable] should clear all subscriptions.
 */
interface Transform<I, O> {
    companion object {
        private val noTransformNoTerminateThrowable = NoTransformThrowable(false)
        private val noTransformTerminateThrowable = NoTransformThrowable(true)
    }

    @Throws
    operator fun invoke(input: I): O

    @Throws
    fun noTransform(terminate: Boolean): Nothing = throw if (terminate) noTransformTerminateThrowable else noTransformNoTerminateThrowable
}
