package com.doofcraft.vessel.common.reactive

import com.doofcraft.vessel.common.collections.PrioritizedList
import com.doofcraft.vessel.common.collections.Priority

/**
 * A straightforward implementation of [Observable] that can emit values but holds no state.
 */
open class SimpleObservable<T> : Observable<T> {
    protected val subscriptions = PrioritizedList<ObservableSubscription<T>>()
    override fun subscribe(priority: Priority, handler: (T) -> Unit): ObservableSubscription<T> {
        val subscription = ObservableSubscription(this, handler)
        subscriptions.add(priority, subscription)
        return subscription
    }

    override fun unsubscribe(subscription: ObservableSubscription<T>) {
        subscriptions.remove(subscription)
    }

    open fun emit(vararg values: T) {
        if (subscriptions.isEmpty()) {
            return
        }
        values.forEach { value ->
            // One or more of these subscriptions might be removed during emission, snapshot handles this.
            val subscriptionsSnapshot = subscriptions.toList()
            subscriptionsSnapshot.forEach { subscription -> subscription.handle(value) }
        }
    }
}
