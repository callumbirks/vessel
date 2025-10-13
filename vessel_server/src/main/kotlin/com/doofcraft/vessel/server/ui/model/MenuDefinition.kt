package com.doofcraft.vessel.server.ui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

@Serializable
class MenuDefinition(
    val title: String,
    val rows: Int,
    @SerialName("open_params") val openParams: Map<String, String>? = null,
    val refresh: Refresh? = null,
    val state: Map<String, String> = emptyMap(),
    val data: Map<String, DataNodeDef> = emptyMap(),
    val widgets: List<WidgetDef> = emptyList()
) {
    @Transient
    lateinit var id: String
        internal set

    @Serializable
    data class Refresh(
        @SerialName("interval_ms") val intervalMs: Long, val nodes: List<String> = emptyList()
    )

    override fun toString(): String {
        return "{ id: $id, title: $title, rows: $rows, openParams: $openParams, refresh: $refresh, data: $data, widgets: $widgets }"
    }
}

@Serializable
data class DataNodeDef(
    val cmd: String, val args: Map<String, JsonElement>? = null, val input: String? = null, val cache: Cache? = null
) {
    @Serializable
    data class Cache(val ttl: Long? = null)
}

@Serializable
sealed class WidgetDef {
    abstract val type: String

    @Serializable
    @SerialName("button")
    data class Button(
        override val type: String = "button",
        val slot: Int,
        val icon: IconDef,
        @SerialName("hide_if") val hideIf: String? = null,
        @SerialName("enabled_if") val enabledIf: String? = null,
        @SerialName("on_click") val onClick: ActionDef? = null
    ) : WidgetDef()

    @Serializable
    @SerialName("label")
    data class Label(
        override val type: String = "label",
        val slot: Int,
        val icon: IconDef,
        @SerialName("hide_if") val hideIf: String? = null,
    ) : WidgetDef()

    @Serializable
    @SerialName("list")
    data class ListWidget(
        override val type: String = "list",
        val layout: Layout,
        val items: Items,
        @SerialName("hide_if") val hideIf: String? = null,
    ) : WidgetDef() {
        @Serializable
        data class Layout(val slots: List<Int>)

        @Serializable
        data class Items(
            val from: String, // data node id
            val icon: IconDef, @SerialName("on_click") val onClick: ActionDef? = null
        )
    }
}

@Serializable
data class IconDef(
    val item: String,
    val name: String? = null,
    val lore: List<String>? = null,
    val replacements: IconReplacements? = null
)

@Serializable
data class IconReplacements(
    val name: Map<String, String> = emptyMap(), val lore: Map<String, String> = emptyMap()
)

@Serializable
data class ActionDef(
    val run: String, val args: Map<String, String>? = null
)