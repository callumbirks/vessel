package com.doofcraft.vessel.client.model

import com.doofcraft.vessel.common.api.VesselBehaviourRegistry
import com.doofcraft.vessel.common.registry.BehaviourComponents
import com.doofcraft.vessel.common.util.vesselTag
import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.Model
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

object VesselArmorRenderer : ArmorRenderer {
    @Volatile
    private var modelsBaked = false
    private lateinit var inner: HumanoidModel<LivingEntity>
    private lateinit var outer: HumanoidModel<LivingEntity>

    private fun ensureModels() {
        if (modelsBaked) return

        modelsBaked = true
        val client = Minecraft.getInstance()
        val models = client.entityModels
        this.inner = HumanoidModel(models.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR))
        this.outer = HumanoidModel(models.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))
    }

    override fun render(
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        stack: ItemStack,
        entity: LivingEntity,
        slot: EquipmentSlot,
        light: Int,
        contextModel: HumanoidModel<LivingEntity>
    ) {
        ensureModels()

        val useInnerModel = usesInnerModel(slot)
        val armorModel = if (useInnerModel) inner else outer
        contextModel.copyPropertiesTo(armorModel)
        setPartVisibility(armorModel, slot)

        val texture = stack.vesselTag()?.let { tag ->
            VesselBehaviourRegistry.get(tag.key, BehaviourComponents.ARMOR_RENDER_TEXTURES)?.let { layers ->
                if (usesInnerModel(slot)) layers.layer2 else layers.layer1
            }
        } ?: return

        ArmorRenderer.renderPart(matrices, vertexConsumers, light, stack, armorModel, texture)
    }

    private fun setPartVisibility(model: HumanoidModel<LivingEntity>, slot: EquipmentSlot) {
        model.setAllVisible(false)
        when (slot) {
            EquipmentSlot.HEAD -> {
                model.head.visible = true; model.hat.visible = true
            }

            EquipmentSlot.CHEST -> {
                model.body.visible = true; model.rightArm.visible = true; model.leftArm.visible = true
            }

            EquipmentSlot.LEGS -> {
                model.body.visible = true; model.rightLeg.visible = true; model.leftLeg.visible = true
            }

            EquipmentSlot.FEET -> {
                model.rightLeg.visible = true; model.leftLeg.visible = true
            }

            else -> {}
        }
    }

    private fun usesInnerModel(slot: EquipmentSlot): Boolean = slot == EquipmentSlot.LEGS

    fun renderPart(matrics: PoseStack, vertexConsumers: MultiBufferSource, light: Int, stack: ItemStack, model: Model, texture: ResourceLocation) {

    }

//    fun renderPart(
//        matrices: PoseStack?,
//        vertexConsumers: MultiBufferSource,
//        light: Int,
//        stack: ItemStack,
//        model: Model,
//        texture: ResourceLocation?
//    ) {
//        val vertexConsumer =
//            ItemRenderer.getArmorFoilBuffer(vertexConsumers, RenderType.armorCutoutNoCull(texture), stack.hasFoil())
//        model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -0x1)
//    }
//    private fun renderModel(
//        poseStack: PoseStack?,
//        bufferSource: MultiBufferSource,
//        packedLight: Int,
//        model: A?,
//        dyeColor: Int,
//        textureLocation: ResourceLocation?
//    ) {
//        val vertexConsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(textureLocation))
//        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, dyeColor)
//    }
//
//    private fun renderTrim(
//        armorMaterial: Holder<ArmorMaterial>?,
//        poseStack: PoseStack?,
//        bufferSource: MultiBufferSource,
//        packedLight: Int,
//        trim: ArmorTrim,
//        model: A?,
//        innerTexture: Boolean
//    ) {
//        val textureAtlasSprite: TextureAtlasSprite =
//            this.armorTrimAtlas.getSprite(
//                if (innerTexture) trim.innerTexture(armorMaterial) else trim.outerTexture(armorMaterial)
//            )
//        val vertexConsumer =
//            textureAtlasSprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())))
//        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY)
//    }
}