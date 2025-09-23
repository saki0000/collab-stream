@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.domain.usecase

import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.plus
import org.example.project.domain.model.VideoDetails
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.model.VideoSyncInfo
import org.example.project.domain.repository.VideoSyncRepository

/**
 * Use case for video synchronization functionality.
 * Handles the business logic for calculating absolute time from video playback position.
 */
interface VideoSyncUseCase {
    /**
     * Synchronizes video playback position to absolute time.
     *
     * Retrieves video details from the specified video service API and calculates the absolute time
     * corresponding to the current playback position by adding the playback seconds
     * to the stream's start time.
     *
     * @param videoId Video ID to synchronize
     * @param currentPlaybackSeconds Current playback position in seconds
     * @param serviceType The video service type (YouTube or Twitch)
     * @return Result containing VideoSyncInfo with calculated absolute time
     */
    suspend fun syncVideoToAbsoluteTime(
        videoId: String,
        currentPlaybackSeconds: Float,
        serviceType: VideoServiceType,
    ): Result<VideoSyncInfo>
}

/**
 * Default implementation of VideoSyncUseCase.
 */
class VideoSyncUseCaseImpl(
    private val videoSyncRepository: VideoSyncRepository,
) : VideoSyncUseCase {

    override suspend fun syncVideoToAbsoluteTime(
        videoId: String,
        currentPlaybackSeconds: Float,
        serviceType: VideoServiceType,
    ): Result<VideoSyncInfo> {
        return try {
            // Validate input parameters
            if (videoId.isBlank()) {
                return Result.failure(IllegalArgumentException("Video ID cannot be blank"))
            }

            if (currentPlaybackSeconds < 0) {
                return Result.failure(IllegalArgumentException("Playback seconds must be non-negative"))
            }

            // Fetch video details from the specified service API
            val videoDetailsResult = videoSyncRepository.getVideoDetails(videoId, serviceType)

            videoDetailsResult.fold(
                onSuccess = { videoDetails ->
                    calculateSyncInfo(videoDetails, videoId, currentPlaybackSeconds)
                },
                onFailure = { error ->
                    Result.failure(error)
                },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateSyncInfo(
        videoDetails: VideoDetails,
        videoId: String,
        currentPlaybackSeconds: Float,
    ): Result<VideoSyncInfo> {
        val streamStartTime = videoDetails.getStartTimeForSync()
            ?: return Result.failure(
                IllegalStateException("Video $videoId does not have start time information for synchronization"),
            )

        // Calculate absolute time by adding playback seconds to stream start time
        val absoluteTime = streamStartTime.plus(currentPlaybackSeconds.toDouble().seconds)

        val syncInfo = VideoSyncInfo(
            videoId = videoId,
            playbackSeconds = currentPlaybackSeconds,
            streamStartTime = streamStartTime,
            absoluteTime = absoluteTime,
        )

        return Result.success(syncInfo)
    }
}
