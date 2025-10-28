package com.doofcraft.vessel.server.api.data

@JvmInline
value class Result<out T> internal constructor(internal val value: Any?) {
    val isSuccess: Boolean get() = value !is Failure
    val isFailure: Boolean get() = value is Failure

    fun get(): T = getOrNull() ?: error { "Attempted to get Success value out of Failure Result." }

    fun getOrNull(): T? =
        when {
            isFailure -> null
            else -> value as T
        }

    fun failure(): String = failureOrNull() ?: error { "Attempted to get Failure value out of Success Result." }

    fun failureOrNull(): String? =
        when {
            isFailure -> (value as Failure).message
            else -> null
        }

    inline fun then(onSuccess: (T) -> Unit = {}, onFailure: (String) -> Unit = {}) {
        getOrNull()?.let { onSuccess(it) }
        failureOrNull()?.let { onFailure(it) }
    }

    companion object {
        fun <T> success(value: T): Result<T> = Result(value)
        fun <T> failure(message: String): Result<T> = Result(Failure(message))
    }

    @JvmInline
    private value class Failure(val message: String)
}
