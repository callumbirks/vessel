package com.doofcraft.vessel.common.serialization

import net.minecraft.network.RegistryFriendlyByteBuf

/**
 * Represents an object that can be encoded to a [RegistryFriendlyByteBuf].
 */
interface Encodable {
    /**
     * Writes this instance to the given buffer.
     *
     * @param buffer The [RegistryFriendlyByteBuf] being written to.
     */
    fun encode(buffer: RegistryFriendlyByteBuf)
}
