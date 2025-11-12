package com.doofcraft.vessel.common.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

data class PacketRegisterInfo<T : NetworkPacket<T>>(
    val id: ResourceLocation,
    val decoder: (RegistryFriendlyByteBuf) -> T,
    val direction: Direction
) {
    val payloadId = CustomPacketPayload.Type<T>(id)
    val codec: StreamCodec<RegistryFriendlyByteBuf, T> = StreamCodec.of({ buf, packet -> packet.encode(buf) }, decoder)

    fun registerPacket() {
        when (direction) {
            Direction.Clientbound -> {
                PayloadTypeRegistry.playS2C().register(payloadId, codec)
            }
            Direction.Serverbound -> {
                PayloadTypeRegistry.playC2S().register(payloadId, codec)
            }
            Direction.Both -> {
                PayloadTypeRegistry.playS2C().register(payloadId, codec)
                PayloadTypeRegistry.playC2S().register(payloadId, codec)
            }
        }
    }

    fun registerClientHandler(handler: ClientNetworkPacketHandler<T>) {
        ClientPlayNetworking.registerGlobalReceiver(payloadId) { obj, _ ->
            handler.handle(obj, Minecraft.getInstance())
        }
    }

    fun registerServerHandler(handler: ServerNetworkPacketHandler<T>) {
        ServerPlayNetworking.registerGlobalReceiver(payloadId) { obj, context ->
            handler.handle(obj, context.player().server, context.player())
        }
    }

    enum class Direction {
        Clientbound,
        Serverbound,
        Both
    }
}
