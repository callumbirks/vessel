package com.doofcraft.vessel.common.reactive

/**
 * A specific exception that allows canceled transformations to occur in pipes.
 */
class NoTransformThrowable(val terminate: Boolean) : Throwable()
