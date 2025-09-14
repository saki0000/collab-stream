@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.domain.usecase

import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import org.example.project.domain.model.VideoSyncInfo
import org.example.project.domain.repository.VideoSyncRepository
import kotlin.time.Duration.Companion.seconds

/**
 * Use case for video synchronization functionality.
 * Handles the business logic for calculating absolute time from video playback position.
 */
interface VideoSyncUseCase {
    /**
     * Synchronizes video playback position to absolute time.
     *
     * Retrieves video details from YouTube API and calculates the absolute time
     * corresponding to the current playback position by adding the playback seconds
     * to the stream's actual start time.
     *
     * @param videoId YouTube video ID to synchronize
     * @param currentPlaybackSeconds Current playback position in seconds
     * @return Result containing VideoSyncInfo with calculated absolute time
     */
    suspend fun syncVideoToAbsoluteTime(
        videoId: String,
        currentPlaybackSeconds: Float
    ): Result<VideoSyncInfo>
}

/**
 * Default implementation of VideoSyncUseCase.
 */
class VideoSyncUseCaseImpl(
    private val videoSyncRepository: VideoSyncRepository
) : VideoSyncUseCase {

    override suspend fun syncVideoToAbsoluteTime(
        videoId: String,
        currentPlaybackSeconds: Float
    ): Result<VideoSyncInfo> {
        return try {
            // Validate input parameters
            if (videoId.isBlank()) {
                return Result.failure(IllegalArgumentException("Video ID cannot be blank"))
            }

            if (currentPlaybackSeconds < 0) {
                return Result.failure(IllegalArgumentException("Playback seconds must be non-negative"))
            }

            // Fetch video details from YouTube API
            val videoDetailsResult = videoSyncRepository.getVideoDetails(videoId)

            videoDetailsResult.fold(
                onSuccess = { videoDetails ->
                    val liveStreamingDetails = videoDetails.liveStreamingDetails
                        ?: return Result.failure(
                            IllegalStateException("Video $videoId does not have live streaming details")
                        )

                    val streamStartTime = liveStreamingDetails.actualStartTime
                        ?: return Result.failure(
                            IllegalStateException("Video $videoId does not have actual start time")
                        )

                    // Calculate absolute time by adding playback seconds to stream start time
                    val absoluteTime = streamStartTime.plus(currentPlaybackSeconds.toDouble().seconds)

                    val syncInfo = VideoSyncInfo(
                        videoId = videoId,
                        playbackSeconds = currentPlaybackSeconds,
                        streamStartTime = streamStartTime,
                        absoluteTime = absoluteTime
                    )

                    Result.success(syncInfo)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}