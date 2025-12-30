package org.example.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlin.time.ExperimentalTime
import org.example.project.domain.model.StreamInfo
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.home.ui.HomeContainer
import org.example.project.feature.streamer_search.ui.StreamerSearchContainer
import org.example.project.feature.video_playback.ui.VideoContainer

/**
 * Main navigation graph for the application.
 *
 * This sets up the NavHost with all app destinations:
 * - HomeRoute: Initial screen with group selection
 * - StreamerSearchRoute: Streamer search bottom sheet (MAIN or SUB mode)
 * - MainPlayerRoute: Main video player screen with sync functionality
 *
 * Uses type-safe navigation with kotlinx.serialization.
 *
 * @param modifier Modifier to be applied to the NavHost
 * @param navController The NavHostController managing navigation state (default: rememberNavController)
 */
@OptIn(ExperimentalTime::class)
@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        // Home screen
        composable<HomeRoute> {
            HomeContainer(
                onSearchMainStreamer = {
                    navController.navigate(StreamerSearchRoute(searchMode = "MAIN"))
                },
                modifier = Modifier,
            )
        }

        // Streamer search bottom sheet (Main or Sub)
        bottomSheet<StreamerSearchRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<StreamerSearchRoute>()

            // Get existing sub streams and main stream ID from previous backstack entry if in SUB mode
            val existingSubStreamIds = if (route.searchMode == "SUB") {
                navController.previousBackStackEntry?.savedStateHandle?.get<List<String>>(
                    "existing_sub_stream_ids",
                ) ?: emptyList()
            } else {
                emptyList()
            }
            val mainStreamId = if (route.searchMode == "SUB") {
                navController.previousBackStackEntry?.savedStateHandle?.get<String>("main_stream_id")
            } else {
                null
            }
            val mainPublishedAt = if (route.searchMode == "SUB") {
                navController.previousBackStackEntry?.savedStateHandle?.get<Long>("main_published_at")
            } else {
                null
            }

            StreamerSearchContainer(
                onDismiss = { navController.popBackStack() },
                existingSubStreamIds = existingSubStreamIds,
                mainStreamId = mainStreamId,
                mainPublishedAt = mainPublishedAt,
                onStreamerSelected = { searchResult, serviceType ->
                    if (route.searchMode == "MAIN") {
                        // Navigate to Main Player with selected main streamer
                        navController.navigate(
                            MainPlayerRoute(
                                mainStreamId = searchResult.videoId,
                                mainChannelId = searchResult.channelTitle, // TODO: Get actual channelId
                                mainChannelName = searchResult.channelTitle,
                                mainServiceType = serviceType.name,
                                mainThumbnailUrl = searchResult.thumbnailUrl,
                                mainTitle = searchResult.title,
                                mainChannelIconUrl = "", // TODO: Get channel icon
                                mainIsLive = searchResult.isLiveBroadcast,
                                mainPublishedAt = searchResult.publishedAt.epochSeconds,
                            ),
                        ) {
                            // Replace the entire back stack
                            popUpTo<HomeRoute> {
                                inclusive = false
                            }
                        }
                    } else {
                        // SUB mode: Pass sub stream result back via SavedStateHandle
                        // Pass individual primitive fields (same pattern as MainPlayerRoute)
                        navController.previousBackStackEntry?.savedStateHandle?.apply {
                            set("sub_stream_id", searchResult.videoId)
                            set("sub_title", searchResult.title)
                            set("sub_thumbnail_url", searchResult.thumbnailUrl)
                            set("sub_channel_id", searchResult.channelTitle) // TODO: Get actual channelId
                            set("sub_channel_name", searchResult.channelTitle)
                            set("sub_channel_icon_url", "") // TODO: Get channel icon
                            set("sub_service_type", serviceType.name)
                            set("sub_published_at", searchResult.publishedAt.epochSeconds)
                            set("sub_is_live", searchResult.isLiveBroadcast)
                        }

                        // Don't close the modal - allow multiple selections
                        // User can close it manually using the Close button in TopAppBar
                    }
                },
                onStreamRemoved = { videoId ->
                    // SUB mode: Notify removal via SavedStateHandle
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("remove_sub_stream_id", videoId)
                    }
                },
                modifier = Modifier,
            )
        }

        // Main Player screen
        composable<MainPlayerRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<MainPlayerRoute>()

            // Reconstruct StreamInfo from navigation parameters
            @OptIn(ExperimentalTime::class)
            val mainStreamInfo = StreamInfo(
                streamId = route.mainStreamId,
                title = route.mainTitle,
                thumbnailUrl = route.mainThumbnailUrl,
                channelId = route.mainChannelId,
                channelName = route.mainChannelName,
                channelIconUrl = route.mainChannelIconUrl,
                serviceType = VideoServiceType.valueOf(route.mainServiceType),
                publishedAt = kotlin.time.Instant.fromEpochSeconds(route.mainPublishedAt),
                isLive = route.mainIsLive,
                currentTime = 0f,
                isSynced = true, // Main is always synced
            )

            VideoContainer(
                modifier = Modifier,
                onNavigateToSubSearch = {
                    // Set main stream info for SUB search to use
                    backStackEntry.savedStateHandle.apply {
                        set("main_published_at", route.mainPublishedAt)
                    }
                    // Navigate to streamer search in SUB mode
                    navController.navigate(StreamerSearchRoute(searchMode = "SUB"))
                },
                mainStreamInfo = mainStreamInfo,
                savedStateHandle = backStackEntry.savedStateHandle,
            )
        }
    }
}
