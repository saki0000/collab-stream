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
import org.koin.compose.viewmodel.koinViewModel

/**
 * Container Composable (Stateful) - Connects to ViewModel and manages state
 * This is the only stateful composable in the hierarchy following the 4-tier pattern:
 * Container -> Screen -> Content -> Component
 *
 * Receives main stream info and handles sub stream selection through SavedStateHandle.
 * Navigation handling is managed by the NavGraph layer for proper separation of concerns.
 */
@Composable
fun VideoContainer(
    modifier: Modifier = Modifier,
    onNavigateToSubSearch: () -> Unit = {},
    mainStreamInfo: StreamInfo? = null,
    savedStateHandle: SavedStateHandle? = null,
    viewModel: VideoViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Process main stream info when received from navigation layer
    LaunchedEffect(mainStreamInfo) {
        mainStreamInfo?.let { streamInfo ->
            viewModel.handleIntent(VideoIntent.LoadMainStream(streamInfo))
        }
    }

    // Process sub stream selection result from SavedStateHandle
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

    // Process sub stream removal from SavedStateHandle
    LaunchedEffect(Unit) {
        savedStateHandle?.let { handle ->
            // Observe removal requests
            handle.getStateFlow<String?>("remove_sub_stream_id", null).collect { removeId ->
                removeId?.let { streamId ->
                    // Remove sub stream from ViewModel
                    viewModel.handleIntent(VideoIntent.RemoveSubStream(streamId))
                    // Clear the removal request after processing
                    handle.set("remove_sub_stream_id", null as String?)
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
        onNavigateToSubSearch = {
            // Set existing sub stream IDs and main stream ID in SavedStateHandle before navigation
            savedStateHandle?.apply {
                set("existing_sub_stream_ids", uiState.subStreams.map { it.streamId })
                set("main_stream_id", uiState.mainStream?.streamId)
            }
            onNavigateToSubSearch()
        },
        modifier = modifier,
    )
}
