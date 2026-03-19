package com.doofcraft.vessel.server.ui

internal class MenuSessionSyncTracker(
    initialSyncId: Int = UNSET_SYNC_ID
) {
    companion object {
        const val UNSET_SYNC_ID = -1
    }

    private val staleSyncIds = LinkedHashSet<Int>()

    var currentSyncId: Int = initialSyncId
        private set

    fun replaceWith(syncId: Int) {
        currentSyncId.takeIf { it != UNSET_SYNC_ID }?.let(staleSyncIds::add)
        currentSyncId = syncId
    }

    fun shouldClose(syncId: Int?): Boolean {
        return when {
            syncId == null -> true
            syncId == currentSyncId -> true
            staleSyncIds.remove(syncId) -> false
            else -> false
        }
    }
}
