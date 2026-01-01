package com.doofcraft.vessel.common.base

import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.Equipable
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DispenserBlock

// A clone of ArmorItem
open class VesselBaseArmor(
    protected val material: Holder<ArmorMaterial>, protected val armorType: ArmorItem.Type, properties: Properties
) : VesselBaseItem(properties), Equipable {
    private val defaultModifiers: () -> ItemAttributeModifiers

    init {
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
        this.defaultModifiers = {
            val defense = material.value().getDefense(armorType)
            val toughness = material.value().toughness
            val builder = ItemAttributeModifiers.builder()
            val equipmentSlotGroup = EquipmentSlotGroup.bySlot(armorType.slot)
            val resourceLocation = ResourceLocation.withDefaultNamespace("armor." + armorType.name.lowercase())
            builder.add(
                Attributes.ARMOR,
                AttributeModifier(resourceLocation, defense.toDouble(), AttributeModifier.Operation.ADD_VALUE),
                equipmentSlotGroup
            )
            builder.add(
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier(resourceLocation, toughness.toDouble(), AttributeModifier.Operation.ADD_VALUE),
                equipmentSlotGroup
            )
            val resistance = material.value().knockbackResistance
            if (resistance > 0f) {
                builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    AttributeModifier(resourceLocation, resistance.toDouble(), AttributeModifier.Operation.ADD_VALUE),
                    equipmentSlotGroup
                )
            }

            builder.build()
        }
    }

    override fun getEnchantmentValue(): Int {
        return this.material.value().enchantmentValue
    }

    override fun isValidRepairItem(stack: ItemStack, repairCandidate: ItemStack): Boolean {
        return this.material.value().repairIngredient.get().test(repairCandidate) || super.isValidRepairItem(
            stack, repairCandidate
        )
    }

    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        return swapWithEquipmentSlot(this, level, player, usedHand)
    }

    @Deprecated("Deprecated in Java")
    override fun getDefaultAttributeModifiers(): ItemAttributeModifiers? {
        return defaultModifiers()
    }

    override fun getEquipmentSlot(): EquipmentSlot? {
        return this.armorType.slot
    }

    override fun getEquipSound(): Holder<SoundEvent?>? {
        return this.material.value().equipSound
    }
}