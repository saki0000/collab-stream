package org.example.project.domain.model

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Data model for a channel in the Timeline Sync screen.
 *
 * Represents a synchronization target channel with its associated stream information
 * and timeline display properties. Used for displaying multiple channels on a unified
 * timeline and calculating seek positions for external app navigation.
 *
 * Epic: Timeline Sync (EPIC-002)
 * Shared across: US-1 (Timeline Display), US-2 (Channel Management),
 *                US-3 (Sync Calculation), US-4 (External App Navigation)
 */
@OptIn(ExperimentalTime::class)
data class SyncChannel(
    // Channel identification
    /**
     * Unique identifier for the channel (platform-specific).
     */
    val channelId: String,

    /**
     * Display name of the channel/streamer.
     */
    val channelName: String,

    /**
     * URL of the channel's icon/avatar image.
     */
    val channelIconUrl: String,

    /**
     * Video service type (YOUTUBE or TWITCH).
     */
    val serviceType: VideoServiceType,

    // Selected stream information (nullable when no stream is selected)
    /**
     * Video ID of the selected stream. Null when no stream is selected for this channel.
     */
    val streamId: String? = null,

    /**
     * Title of the selected stream.
     */
    val streamTitle: String? = null,

    /**
     * Thumbnail URL of the selected stream.
     */
    val streamThumbnailUrl: String? = null,

    // Timeline display properties
    /**
     * Absolute start time of the stream (when the stream began).
     * Used for timeline positioning and sync calculations.
     */
    val streamStartTime: Instant? = null,

    /**
     * Absolute end time of the stream (when the stream ended).
     * Null for live streams or when duration is unknown.
     */
    val streamEndTime: Instant? = null,

    /**
     * Duration of the stream.
     * Can be calculated from start/end times or fetched from API.
     */
    val streamDuration: Duration? = null,

    // Synchronization state
    /**
     * Current synchronization status.
     * Determines whether the Open/Wait button is displayed.
     */
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,

    /**
     * Calculated seek position in seconds for external app navigation.
     * Set when sync calculation is performed based on the global sync time.
     */
    val targetSeekPosition: Float? = null,
)

/**
 * Convenience function to check if this channel has a stream selected.
 */
@OptIn(ExperimentalTime::class)
fun SyncChannel.hasStream(): Boolean = streamId != null

/**
 * Convenience function to check if this channel is ready to open in external app.
 */
@OptIn(ExperimentalTime::class)
fun SyncChannel.isOpenable(): Boolean = syncStatus == SyncStatus.READY && targetSeekPosition != null
