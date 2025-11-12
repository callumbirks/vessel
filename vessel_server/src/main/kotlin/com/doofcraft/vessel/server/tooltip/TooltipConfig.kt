package com.doofcraft.vessel.server.tooltip

import com.doofcraft.vessel.common.tooltip.TooltipRegistry
import com.doofcraft.vessel.common.util.vesselResource
import com.doofcraft.vessel.server.api.config.ConfigFactory
import com.doofcraft.vessel.server.api.data.Result
import com.doofcraft.vessel.server.api.reactive.SimpleObservable
import com.doofcraft.vessel.server.util.toText
import de.themoep.minedown.adventure.MineDown
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

object TooltipConfig : ConfigFactory<Map<String, List<String>>> {
    override val id = vesselResource("tooltips")
    override val observable = SimpleObservable<Map<String, List<String>>>()
    override val json = Json
    override val serializer = MapSerializer(String.serializer(), ListSerializer(String.serializer()))

    override lateinit var CONFIG: Map<String, List<String>>

    override fun reload(config: Map<String, List<String>>) {
        val tooltips = config.mapValues { (_, list) -> list.map { MineDown(it).toComponent().toText() } }
        TooltipRegistry.reloadFromServer(tooltips)
        observable.emit(config)
    }

    override fun validate(config: Map<String, List<String>>): Result<Unit> {
        return Result.success(Unit)
    }

    override fun default(): Map<String, List<String>> =
        mapOf("items.battleaxe" to listOf("&green&Swing it at foes for fun!"))
}