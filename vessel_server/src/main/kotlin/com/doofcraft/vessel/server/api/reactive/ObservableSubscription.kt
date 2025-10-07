package com.doofcraft.vessel.server.api.reactive

class ObservableSubscription<T>(
    private val observable: Observable<T>,
    private val handler: (T) -> Unit
) {
    var alive = true
    fun handle(value: T) = handler(value)
    fun unsubscribe() {
        observable.unsubscribe(this)
        alive = false
    }
}
