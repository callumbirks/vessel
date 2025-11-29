package com.doofcraft.vessel.server.api.async

import com.doofcraft.vessel.common.VesselMod
import com.doofcraft.vessel.server.VesselServer
import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object VesselAsync {
    val Dispatcher = Executors.newFixedThreadPool(
        2, ThreadFactoryBuilder().setNameFormat("vessel-async-#%d").setDaemon(true).build()
    ).asCoroutineDispatcher()

    val Scope = CoroutineScope(SupervisorJob() + Dispatcher)

    fun <T> launchAsync(block: suspend () -> T): Deferred<T> {
        return Scope.async(Dispatcher) {
            block()
        }
    }

    fun launch(block: suspend () -> Unit) {
        Scope.launch(Dispatcher) {
            block()
        }
    }

    fun launchAfter(delay: Duration, block: suspend () -> Unit) {
        Scope.launch(Dispatcher) {
            delay(delay)
            block()
        }
    }

    fun launchWhen(
        condition: () -> Boolean,
        timeout: Duration = 10.seconds,
        onTimeout: (() -> Unit)? = null,
        block: suspend () -> Unit,
    ) {
        Scope.launch(Dispatcher) {
            val timeoutMillis = timeout.inWholeMilliseconds
            val startTime = System.currentTimeMillis()
            while (!condition()) {
                if (System.currentTimeMillis() - startTime > timeoutMillis) {
                    onTimeout?.invoke() ?: run {
                        VesselMod.LOGGER.warn("VesselAsync.launchWhen condition not met within timeout of ${timeoutMillis}ms")
                    }
                    return@launch
                }
                block()
            }
        }
    }

    fun isMainThread(): Boolean {
        return VesselServer.server.isSameThread
    }

    suspend fun <T> runOnMainThread(block: () -> T): T {
        return if (isMainThread()) {
            block()
        } else {
            withContext(VesselServer.server.asCoroutineDispatcher()) {
                block()
            }
        }
    }

    internal fun shutdown() {
        Dispatcher.close()
    }
}