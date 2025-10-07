package com.doofcraft.vessel.server.util

import com.doofcraft.vessel.server.serialization.TextComponentSerializer
import net.kyori.adventure.text.Component as AdvComponent
import net.minecraft.network.chat.Component as McComponent

fun McComponent.toComponent(): AdvComponent {
    return TextComponentSerializer().deserialize(this)
}

fun AdvComponent.toText(): McComponent {
    return TextComponentSerializer().serialize(this)
}