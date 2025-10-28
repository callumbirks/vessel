package com.doofcraft.vessel.server.api.data

@JvmInline
value class Either<out Left, out Right> internal constructor(internal val value: Any?) {
    val isLeft: Boolean get() = value !is Other<*>
    val isRight: Boolean get() = value is Other<*>

    fun getLeft(): Left = getLeftOrNull() ?: error { "Attempted to get Left out of Right value."}

    fun getLeftOrNull(): Left? =
        when {
            isRight -> null
            else -> value as Left
        }

    fun getRight(): Left = getLeftOrNull() ?: error { "Attempted to get Right out of Left value."}

    fun getRightOrNull(): Right? =
        when {
            isRight -> (value as Other<Right>).value
            else -> null
        }

    fun then(ifLeft: (Left) -> Unit = {}, ifRight: (Right) -> Unit = {}) {
        when {
            isRight -> ifRight((value as Other<Right>).value)
            else -> ifLeft(value as Left)
        }
    }

    companion object {
        fun <L, R> left(value: L): Either<L, R> = Either(value)
        fun <L, R> right(value: R): Either<L, R> = Either(Other(value))
    }

    @JvmInline
    private value class Other<T>(val value: T)
}