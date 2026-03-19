package com.doofcraft.vessel.server.ui

import com.doofcraft.vessel.server.ui.model.MenuDefinition
import net.minecraft.resources.ResourceLocation
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MenuRegistryTest {
    @AfterTest
    fun tearDown() {
        MenuRegistry.reload(emptyMap())
    }

    @Test
    fun `normalizeId leaves namespaced ids unchanged`() {
        assertEquals("vessel:foo", MenuRegistry.normalizeId("vessel:foo"))
    }

    @Test
    fun `normalizeId prefixes bare ids with vessel namespace`() {
        assertEquals("vessel:foo", MenuRegistry.normalizeId("foo"))
    }

    @Test
    fun `reload stores menus by fully qualified resource location`() {
        val vesselMenu = MenuDefinition(title = "Vessel", rows = 1)
        val otherMenu = MenuDefinition(title = "Other", rows = 1)

        MenuRegistry.reload(
            mapOf(
                ResourceLocation.fromNamespaceAndPath("vessel", "foo") to vesselMenu,
                ResourceLocation.fromNamespaceAndPath("other", "foo") to otherMenu
            )
        )

        assertNotNull(MenuRegistry.get("vessel:foo"))
        assertNotNull(MenuRegistry.get("other:foo"))
        assertNull(MenuRegistry.get("missing:foo"))
        assertEquals("vessel:foo", vesselMenu.id)
        assertEquals("other:foo", otherMenu.id)
    }
}
