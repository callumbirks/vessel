package com.doofcraft.vessel.server.ui.handler

import net.minecraft.world.inventory.ClickType
import kotlin.test.Test
import kotlin.test.assertEquals

class UiSlotClickPolicyTest {
    @Test
    fun `pickup on button with empty cursor triggers the button`() {
        val decision = UiSlotClickPolicy.decide(
            slotId = 3,
            menuSize = 9,
            clickType = ClickType.PICKUP,
            carriedEmpty = true,
            hasButton = true
        )

        assertEquals(UiSlotClickDecision.TRIGGER_BUTTON, decision)
    }

    @Test
    fun `pickup with carried stack on ui slot is rejected`() {
        val decision = UiSlotClickPolicy.decide(
            slotId = 3,
            menuSize = 9,
            clickType = ClickType.PICKUP,
            carriedEmpty = false,
            hasButton = true
        )

        assertEquals(UiSlotClickDecision.REJECT, decision)
    }

    @Test
    fun `swap throw clone and pickup all are rejected on ui slots`() {
        val clickTypes = listOf(ClickType.SWAP, ClickType.THROW, ClickType.CLONE, ClickType.PICKUP_ALL)

        clickTypes.forEach { clickType ->
            val decision = UiSlotClickPolicy.decide(
                slotId = 3,
                menuSize = 9,
                clickType = clickType,
                carriedEmpty = true,
                hasButton = true
            )

            assertEquals(UiSlotClickDecision.REJECT, decision)
        }
    }

    @Test
    fun `player inventory clicks pass through`() {
        val decision = UiSlotClickPolicy.decide(
            slotId = 12,
            menuSize = 9,
            clickType = ClickType.PICKUP,
            carriedEmpty = true,
            hasButton = true
        )

        assertEquals(UiSlotClickDecision.PASS_THROUGH, decision)
    }

    @Test
    fun `outside clicks pass through`() {
        val decision = UiSlotClickPolicy.decide(
            slotId = -999,
            menuSize = 9,
            clickType = ClickType.PICKUP,
            carriedEmpty = true,
            hasButton = false
        )

        assertEquals(UiSlotClickDecision.PASS_THROUGH, decision)
    }
}
