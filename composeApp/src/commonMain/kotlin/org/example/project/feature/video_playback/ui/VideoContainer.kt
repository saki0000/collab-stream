package org.example.project.feature.video_playback.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import kotlin.time.ExperimentalTime
import org.example.project.domain.model.StreamInfo
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_playback.VideoIntent
import org.example.project.feature.video_playback.VideoSideEffect
import org.example.project.feature.video_playback.VideoViewModel
import org.example.project.feature.video_search.VideoSelectionResult
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container Composable (Stateful) - Connects to ViewModel and manages state
 * This is the only stateful composable in the hierarchy following the 4-tier pattern:
 * Container -> Screen -> Content -> Component
 *
 * Receives video selection results from navigation layer (passed as parameter).
 * Navigation handling is managed by the NavGraph layer for proper separation of concerns.
 */
@Composable
fun VideoContainer(
    onNavigateToSearch: (initialQuery: String) -> Unit,
    videoSelectionResult: VideoSelectionResult?,
    modifier: Modifier = Modifier,
    onNavigateToSubSearch: () -> Unit = {}, // New parameter for sub search
    mainStreamInfo: StreamInfo? = null, // New parameter for main stream
    savedStateHandle: SavedStateHandle? = null, // For receiving sub stream results
    viewModel: VideoViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Process main stream info when received from navigation layer (new flow)
    LaunchedEffect(mainStreamInfo) {
        mainStreamInfo?.let { streamInfo ->
            viewModel.handleIntent(VideoIntent.LoadMainStream(streamInfo))
        }
    }

    // Process video selection result when received from navigation layer (legacy flow)
    LaunchedEffect(videoSelectionResult) {
        videoSelectionResult?.let { result ->
            // Load the selected video
            viewModel.handleIntent(
                VideoIntent.LoadVideoWithService(result.videoId, result.serviceType),
            )
        }
    }

    // Process sub stream selection result from SavedStateHandle (new flow)
    LaunchedEffect(Unit) {
        savedStateHandle?.let { handle ->
            // Observe sub stream fields from navigation result
            handle.getStateFlow<String?>("sub_stream_id", null).collect { subStreamId ->
                subStreamId?.let { streamId ->
                    // Reconstruct StreamInfo from individual fields (same pattern as MainPlayerRoute)
                    @OptIn(ExperimentalTime::class)
                    val streamInfo = StreamInfo(
                        streamId = streamId,
                        title = handle.get<String>("sub_title") ?: "",
                        thumbnailUrl = handle.get<String>("sub_thumbnail_url") ?: "",
                        channelId = handle.get<String>("sub_channel_id") ?: "",
                        channelName = handle.get<String>("sub_channel_name") ?: "",
                        channelIconUrl = handle.get<String>("sub_channel_icon_url") ?: "",
                        serviceType = VideoServiceType.valueOf(handle.get<String>("sub_service_type") ?: "YOUTUBE"),
                        publishedAt = kotlin.time.Instant.fromEpochSeconds(handle.get<Long>("sub_published_at") ?: 0L),
                        isLive = handle.get<Boolean>("sub_is_live") ?: false,
                        currentTime = 0f,
                        isSynced = false, // Will be synced after adding
                    )

                    // Add sub stream to ViewModel
                    viewModel.handleIntent(VideoIntent.AddSubStream(streamInfo))
                }
            }
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is VideoSideEffect.ShowError -> {
                    snackBarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Short,
                    )
                }

                is VideoSideEffect.ShowSuccess -> {
                    snackBarHostState.showSnackbar(
                        message = sideEffect.message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short,
                    )
                }

                is VideoSideEffect.ShowSyncResult -> {
                    snackBarHostState.showSnackbar(
                        message = "Synchronized to: ${sideEffect.absoluteTime}",
                        actionLabel = "OK",
                        duration = SnackbarDuration.Long,
                    )
                }

                is VideoSideEffect.ShowSyncError -> {
                    snackBarHostState.showSnackbar(
                        message = "Sync Error: ${sideEffect.message}",
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Long,
                    )
                }
            }
        }
    }

    VideoScreen(
        uiState = uiState,
        onIntent = viewModel::handleIntent,
        onVideoError = viewModel::handleVideoError,
        snackbarHostState = snackBarHostState,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToSubSearch = onNavigateToSubSearch,
        modifier = modifier,
    )
}
