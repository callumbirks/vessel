package com.doofcraft.vessel.server.ui.data

import com.doofcraft.vessel.server.ui.cmd.CommandBus
import com.doofcraft.vessel.server.ui.cmd.NodeCache
import com.doofcraft.vessel.server.ui.cmd.UiContext
import com.doofcraft.vessel.server.ui.expr.ExprEngine
import com.doofcraft.vessel.server.ui.expr.JsonTemplater
import com.doofcraft.vessel.server.ui.model.DataNodeDef
import com.doofcraft.vessel.server.ui.model.MenuDefinition
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement

data class DataPlan(
    val def: MenuDefinition,
    val order: List<String>,
    val nodes: Map<String, DataNodeDef>
)

object DataPlanner {
    fun compile(def: MenuDefinition): DataPlan {
        val nodes = def.data
        // topological sort AKA dependencies (from 'node.input') are sorted before their dependents
        val visited = HashSet<String>()
        val out = ArrayList<String>()
        fun dfs(n: String) {
            if (!visited.add(n)) return
            nodes[n]?.input?.let { upstream -> if (nodes.containsKey(upstream)) dfs(upstream) }
            out.add(n)
        }
        nodes.keys.forEach(::dfs)
        return DataPlan(def, out, nodes)
    }
}

class DataExecutor(
    private val engine: ExprEngine
) {
    suspend fun executeAll(
        plan: DataPlan,
        ctx: UiContext,
        cache: NodeCache,
    ) = coroutineScope {
        for (nodeId in plan.order) {
            val node = plan.nodes.getValue(nodeId)
            val ttl = node.cache?.ttl ?: 0L
            val cached = if (ttl > 0) cache.get(nodeKey(ctx, nodeId)) else null
            if (cached != null) {
                ctx.nodeValues[nodeId] = cached
                continue
            }
            val inputValue = resolveInput(node.input, ctx)
            val resolvedArgs = resolveArgs(node.args, ctx)
            val cmd = CommandBus.get(node.cmd)

            val value = cmd.run(ctx, inputValue, resolvedArgs)

            if (value != null) ctx.nodeValues[nodeId] = value
            else ctx.nodeValues.remove(nodeId)

            if (ttl > 0) cache.put(nodeKey(ctx, nodeId), value, ttl)
        }
    }

    private fun nodeKey(ctx: UiContext, nodeId: String) = "${ctx.menuId}:${ctx.playerUuid}:$nodeId"

    private fun resolveInput(
        input: String?,
        ctx: UiContext
    ): Any? {
        if (input == null) return null
        return JsonTemplater.templatizeString(input, engine, ctx.toScope())
    }

    private fun resolveArgs(
        args: Map<String, JsonElement>?,
        ctx: UiContext
    ): Map<String, Any?> {
        if (args == null) return emptyMap()
        val templatized = JsonTemplater.templatizeMapValues(args, engine, ctx.toScope())
        return templatized
    }
}