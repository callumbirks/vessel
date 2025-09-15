package com.doofcraft.vessel.registry

import com.doofcraft.vessel.base.VesselBaseBlock
import com.doofcraft.vessel.util.registry.SimpleRegistry
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.block.enums.NoteBlockInstrument
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry

object ModBlocks : SimpleRegistry<Registry<Block>, Block>() {
    override val registry: Registry<Block> = Registries.BLOCK

    val VESSEL = create(
        "block", VesselBaseBlock(
            AbstractBlock.Settings.create().mapColor(MapColor.EMERALD_GREEN).strength(0.8f).nonOpaque().solidBlock(
                Blocks::never
            )
        )
    )
}