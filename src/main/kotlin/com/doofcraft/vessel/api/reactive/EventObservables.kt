package com.doofcraft.vessel.api.reactive

import com.doofcraft.vessel.api.events.Cancelable

open class EventObservable<T> : SimpleObservable<T>() {
    inline fun post(vararg events: T, then: (T) -> Unit = {}) {
        // Issuing generic-typed pass-throughs to varargs doesn't work
        // properly unless the source is a vararg as well, complicated story.
        emit(*events)
        events.forEach(then)
    }
}

open class CancelableObservable<T : Cancelable> : EventObservable<T>() {
    override fun emit(vararg values: T) {
        if (subscriptions.isEmpty()) {
            return
        }

        values.forEach { value ->
            // One or more of these subscriptions might be removed during emission, snapshot handles this.
            val subscriptionsSnapshot = subscriptions.toList()
            // Stop emitting a value once the value is canceled.
            subscriptionsSnapshot.firstOrNull { subscription ->
                subscription.handle(value)
                return@firstOrNull value.isCanceled
            }
        }
    }

    inline fun postThen(event: T, ifCanceled: (T) -> Unit = {}, ifSucceeded: (T) -> Unit) {
        post(event) {
            if (it.isCanceled) {
                ifCanceled(it)
            } else {
                ifSucceeded(it)
            }
        }
    }
}
