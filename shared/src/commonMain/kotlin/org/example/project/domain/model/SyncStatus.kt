package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * Enum representing the synchronization status of a channel in Timeline Sync.
 *
 * Used by SyncChannel to track the current state of synchronization
 * for external app navigation.
 *
 * Epic: Timeline Sync (EPIC-002)
 */
@Serializable
enum class SyncStatus {
    /**
     * Sync calculation has not been performed yet.
     * Initial state when a channel is added to the timeline.
     */
    NOT_SYNCED,

    /**
     * Waiting for stream to start.
     * The specified sync time is before the stream's start time.
     */
    WAITING,

    /**
     * Sync calculation complete and ready to open.
     * The Open button can be pressed to navigate to the external app.
     */
    READY,

    /**
     * External app has been opened with the calculated seek position.
     * User has tapped the Open button.
     */
    OPENED,
}
