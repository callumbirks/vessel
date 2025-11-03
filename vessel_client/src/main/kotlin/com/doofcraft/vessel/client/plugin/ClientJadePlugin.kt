package com.doofcraft.vessel.client.plugin

import com.doofcraft.vessel.common.base.VesselBaseBlockEntity
import com.doofcraft.vessel.common.component.VesselTag
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaPlugin

class ClientJadePlugin : IWailaPlugin {
    override fun registerClient(registration: IWailaClientRegistration) {
        registration.addItemModNameCallback {
            if (it.has(VesselTag.COMPONENT)) "Doofcraft"
            else null
        }
        registration.addRayTraceCallback { _, accessor, _ ->
            if (accessor is BlockAccessor) {
                val entity = accessor.blockEntity as? VesselBaseBlockEntity
                if (entity != null) {
                    return@addRayTraceCallback registration.blockAccessor().from(accessor).fakeBlock(entity.item).build()
                }
            }
            accessor
        }
    }

//    private object VesselBlockProvider : IBlockComponentProvider {
//        override fun appendTooltip(
//            tooltip: ITooltip,
//            accessor: BlockAccessor,
//            config: IPluginConfig
//        ) {
//            val entity = accessor.blockEntity as? VesselBaseBlockEntity ?: return
//            val tag = entity.get(VesselTag.COMPONENT) ?: return
//            tooltip.replace(
//                JadeIds.CORE_OBJECT_NAME,
//                Component.translatable("item.${tag.key}").withColor(CommonColors.WHITE)
//            )
//            tooltip.replace(JadeIds.CORE_MOD_NAME, Component.literal("doofcraft").withStyle { style ->
//                style.withColor(CommonColors.BLUE).withItalic(true)
//            })
//        }
//
//        override fun getIcon(accessor: BlockAccessor, config: IPluginConfig, currentIcon: IElement): IElement? {
//            val entity = accessor.blockEntity as? VesselBaseBlockEntity ?: return null
//            return ItemStackElement.of(entity.item)
//        }
//
//        override fun getDefaultPriority(): Int {
//            return TooltipPosition.HEAD
//        }
//
//        override fun getUid(): ResourceLocation = ID
//
//        val ID = vesselResource("block_provider")
//    }
}