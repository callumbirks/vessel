package com.doofcraft.vessel.server

import VesselMod.LOGGER
import com.doofcraft.vessel.common.util.vesselResource
import com.doofcraft.vessel.server.api.data.DataRegistry
import com.doofcraft.vessel.server.api.events.VesselEvents
import com.doofcraft.vessel.server.registry.DataProvider
import com.doofcraft.vessel.server.ui.MenuRegistry
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object VesselDataProvider : DataProvider {

    // Both Forge n Fabric keep insertion order so if a registry depends on another simply register it after
    private val registries = linkedSetOf<DataRegistry>()
    private val reloadableRegistries = linkedSetOf<DataRegistry>()
    private val synchronizedPlayerIds = mutableListOf<UUID>()

    private val scheduledActions = mutableMapOf<UUID, MutableList<() -> Unit>>()

    fun registerDefaults() {
        register(MenuRegistry, reloadable = true)

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
            VesselReloadListener(
                vesselResource("data_resources"), SimpleResourceReloader(PackType.SERVER_DATA), emptyList()
            )
        )
    }

    override fun <T : DataRegistry> register(registry: T, reloadable: Boolean): T {
        // Only send message once
        if (this.registries.isEmpty()) {
            LOGGER.info("Note: Vessel data registries are only loaded once per server instance as Pokémon species are not safe to reload.")
        }
        this.registries.add(registry)
        if (reloadable) {
            this.reloadableRegistries.add(registry)
        }
        LOGGER.info("Registered the {} registry", registry.id.toString())
        LOGGER.debug("Registered the {} registry of class {}", registry.id.toString(), registry::class.qualifiedName)
        return registry
    }

    override fun fromIdentifier(registryIdentifier: ResourceLocation): DataRegistry? =
        this.registries.find { it.id == registryIdentifier }

    override fun sync(player: ServerPlayer) {
        if (!player.connection.connection.isMemoryConnection) {
            this.registries.forEach { registry ->
                registry.sync(player)
            }
        }

        VesselEvents.DATA_SYNCHRONIZED.emit(player)
        val waitingActions = this.scheduledActions.remove(player.uuid) ?: return
        waitingActions.forEach { it() }
    }

    override fun doAfterSync(player: ServerPlayer, action: () -> Unit) {
        if (player.uuid in synchronizedPlayerIds) {
            action()
        } else {
            this.scheduledActions.computeIfAbsent(player.uuid) { mutableListOf() }.add(action)
        }
    }

    private class SimpleResourceReloader(private val type: PackType) : ResourceManagerReloadListener {
        override fun onResourceManagerReload(manager: ResourceManager) {
            // Check for a server running, this is due to the create a world screen triggering datapack reloads, these are fine to happen as many times as needed as players may be in the process of adding their datapacks.
            val reloadAllowed = !VesselServer.server.isReady
            registries.filter { it.type == this.type && (reloadAllowed || it in reloadableRegistries) }
                .forEach { it.reload(manager) }
        }
    }

    private class VesselReloadListener(
        private val identifier: ResourceLocation,
        private val reloader: PreparableReloadListener,
        private val dependencies: Collection<ResourceLocation>
    ) : IdentifiableResourceReloadListener {

        override fun reload(
            synchronizer: PreparableReloadListener.PreparationBarrier,
            manager: ResourceManager,
            prepareProfiler: ProfilerFiller,
            applyProfiler: ProfilerFiller,
            prepareExecutor: Executor,
            applyExecutor: Executor
        ): CompletableFuture<Void> =
            this.reloader.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor)

        override fun getFabricId(): ResourceLocation = this.identifier

        override fun getName(): String = this.reloader.name

        override fun getFabricDependencies(): MutableCollection<ResourceLocation> = this.dependencies.toMutableList()
    }
}
