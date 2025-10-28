package com.doofcraft.vessel.server.util

import com.doofcraft.vessel.server.ui.text.TextComponentSerializer
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.Component as AdvComponent
import net.minecraft.network.chat.Component as McComponent

fun McComponent.toComponent(): AdvComponent {
    return TextComponentSerializer().deserialize(this)
}

fun AdvComponent.toText(): McComponent {
    return TextComponentSerializer().serialize(this)
}

fun AdvComponent.isEmpty(): Boolean {
    return when (this) {
        is TextComponent -> {
            this.content().isEmpty() && this.children().all { it.isEmpty() }
        }

        is TranslatableComponent -> {
            false
        }

        else -> {
            this.children().all { it.isEmpty() }
        }
    }
}