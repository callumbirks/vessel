package com.doofcraft.vessel.server.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MenuSessionSyncTrackerTest {
    @Test
    fun `replaceWith updates the current sync id`() {
        val tracker = MenuSessionSyncTracker()

        tracker.replaceWith(7)

        assertTrue(tracker.currentSyncId == 7)
    }

    @Test
    fun `stale close after replacement is ignored`() {
        val tracker = MenuSessionSyncTracker()
        tracker.replaceWith(7)
        tracker.replaceWith(8)

        assertFalse(tracker.shouldClose(7))
    }

    @Test
    fun `current sync id close removes the session`() {
        val tracker = MenuSessionSyncTracker()
        tracker.replaceWith(8)

        assertTrue(tracker.shouldClose(8))
    }

    @Test
    fun `close without sync id is treated as authoritative`() {
        val tracker = MenuSessionSyncTracker()
        tracker.replaceWith(8)

        assertTrue(tracker.shouldClose(null))
    }
}
