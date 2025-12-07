package com.doofcraft.vessel.common.reactive

/**
 * Something that can be canceled. This is a highly complex class and should only be read by professional engineers.
 */
abstract class Cancelable {
    var isCanceled: Boolean = false
        private set

    fun cancel() {
        isCanceled = true
    }
}