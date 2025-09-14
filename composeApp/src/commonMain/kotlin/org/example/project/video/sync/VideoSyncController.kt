@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.video.sync

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.domain.usecase.VideoSyncUseCase
import org.example.project.video.VideoSyncUiState

/**
 * Controller for video synchronization functionality.
 * Handles UI operations and state management for video sync features.
 */
interface VideoSyncController {
    /**
     * Handles sync button click action.
     * Retrieves current playback position and synchronizes to absolute time.
     */
    suspend fun handleSyncButtonClick(videoId: String): Result<VideoSyncUiState>
}

/**
 * Default implementation of VideoSyncController.
 */
class VideoSyncControllerImpl(
    private val videoSyncUseCase: VideoSyncUseCase,
    private val playbackPositionProvider: PlaybackPositionProvider,
) : VideoSyncController {

    override suspend fun handleSyncButtonClick(videoId: String): Result<VideoSyncUiState> {
        return try {
            if (videoId.isBlank()) {
                return Result.failure(IllegalArgumentException("Video ID is required for sync"))
            }

            // Get current playback position from platform-specific provider
            val playbackPositionResult = playbackPositionProvider.getCurrentPlaybackPosition()

            playbackPositionResult.fold(
                onSuccess = { currentPlaybackSeconds ->
                    // Use VideoSyncUseCase to calculate absolute time
                    val syncResult = videoSyncUseCase.syncVideoToAbsoluteTime(
                        videoId = videoId,
                        currentPlaybackSeconds = currentPlaybackSeconds,
                    )

                    syncResult.fold(
                        onSuccess = { syncInfo ->
                            // Convert to UI state with formatted time
                            val formattedAbsoluteTime = formatAbsoluteTime(syncInfo.absoluteTime)

                            val uiState = VideoSyncUiState(
                                videoId = syncInfo.videoId,
                                playbackSeconds = syncInfo.playbackSeconds,
                                streamStartTime = syncInfo.streamStartTime,
                                absoluteTime = syncInfo.absoluteTime,
                                formattedAbsoluteTime = formattedAbsoluteTime,
                            )

                            Result.success(uiState)
                        },
                        onFailure = { error ->
                            Result.failure(error)
                        },
                    )
                },
                onFailure = { error ->
                    Result.failure(Exception("Failed to get playback position: ${error.message}", error))
                },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatAbsoluteTime(absoluteTime: kotlinx.datetime.Instant): String {
        val localDateTime = absoluteTime.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(
            2,
            '0',
        )}:${localDateTime.second.toString().padStart(2, '0')}"
    }
}
