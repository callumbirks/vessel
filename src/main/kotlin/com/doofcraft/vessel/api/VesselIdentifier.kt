package com.doofcraft.vessel.api

import com.doofcraft.vessel.VesselMod
import net.minecraft.util.Identifier
import net.minecraft.util.InvalidIdentifierException

data class VesselIdentifier(
    val namespace: String? = null,
    val path: String,
) {
    override fun toString(): String {
        return "${namespace ?: VesselMod.MODID}:$path"
    }

    fun toIdentifier(): Identifier {
        return Identifier.of(namespace ?: VesselMod.MODID, path)
    }

    companion object {
        fun tryParse(id: String): VesselIdentifier? {
            if (id.contains(':')) {
                val ns = id.substringBefore(':')
                val path = id.substringAfter(':', "")
                if (ns.isEmpty() || path.isEmpty()) return null
                return VesselIdentifier(ns, path)
            }
            if (id.isEmpty()) return null
            return VesselIdentifier(null, id)
        }

        fun parse(id: String): VesselIdentifier {
            return tryParse(id) ?: throw InvalidIdentifierException("Invalid VesselIdentifier")
        }

        fun of(namespace: String, path: String): VesselIdentifier {
            return VesselIdentifier(namespace, path)
        }

        fun vessel(key: String): VesselIdentifier {
            return VesselIdentifier(null, key)
        }
    }
}