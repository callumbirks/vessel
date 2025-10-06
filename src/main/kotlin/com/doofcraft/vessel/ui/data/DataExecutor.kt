package com.doofcraft.vessel.ui.data

import com.doofcraft.vessel.ui.cmd.CommandBus
import com.doofcraft.vessel.ui.cmd.NodeCache
import com.doofcraft.vessel.ui.cmd.UiContext
import com.doofcraft.vessel.ui.expr.ExprEngine
import com.doofcraft.vessel.ui.expr.JsonTemplater
import com.doofcraft.vessel.ui.expr.Scope
import com.doofcraft.vessel.ui.model.DataNodeDef
import com.doofcraft.vessel.ui.model.MenuDefinition
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonElement

class DataPlan(
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
        return _root_ide_package_.com.doofcraft.vessel.ui.data.DataPlan(def, out, nodes)
    }
}

class DataExecutor(
    private val engine: ExprEngine
) {
    suspend fun executeAll(
        plan: DataPlan,
        ctx: UiContext,
        cache: NodeCache,
        existingValues: Map<String, Any?> = emptyMap()
    ): Map<String, Any?> = coroutineScope {
        val results = existingValues.toMutableMap()
        for (nodeId in plan.order) {
            val node = plan.nodes.getValue(nodeId)
            val ttl = node.cache?.ttl ?: 0L
            val cached = if (ttl > 0) cache.get(nodeKey(ctx, nodeId)) else null
            if (cached != null) {
                results[nodeId] = cached
                continue
            }
            val inputValue = node.input?.let { results[it] }
            val resolvedArgs = resolveArgs(node.args, plan, results, ctx)
            val cmd = CommandBus.get(node.cmd)
            val value = cmd.run(ctx, inputValue, resolvedArgs)
            results[nodeId] = value
            if (ttl > 0) cache.put(nodeKey(ctx, nodeId), value, ttl)
        }
        results
    }

    private fun nodeKey(ctx: UiContext, nodeId: String) = "${ctx.menuId}:${ctx.playerUuid}:$nodeId"

    private fun resolveArgs(
        args: JsonElement?,
        plan: DataPlan,
        nodeValues: Map<String, Any?>,
        ctx: UiContext
    ): Any? {
        if (args == null) return null
        val scope = Scope(
            menu = mapOf("id" to plan.def.id),
            player = mapOf("uuid" to ctx.playerUuid),
            nodeValues = nodeValues,
            state = ctx.state
        )
        return JsonTemplater.templatize(args, engine, scope)
    }
}