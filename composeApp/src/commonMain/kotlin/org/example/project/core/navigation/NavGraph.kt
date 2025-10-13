package org.example.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.example.project.feature.video_playback.ui.VideoContainer
import org.example.project.feature.video_search.VideoSelectionResult
import org.example.project.feature.video_search.ui.VideoSearchContainer

/**
 * Main navigation graph for the application.
 *
 * This sets up the NavHost with all app destinations:
 * - HomeRoute: Main video player screen
 * - VideoSearchRoute: Video search bottom sheet (managed as a navigation destination)
 *
 * Uses hybrid navigation approach:
 * - Forward: Navigation Arguments (VideoSearchRoute with initialQuery)
 * - Backward: SavedStateHandle with VideoSelectionResult (managed at navigation layer)
 *
 * Navigation result handling is centralized here to maintain proper separation of concerns.
 * Feature-level components (VideoContainer) remain unaware of navigation implementation details.
 *
 * @param modifier Modifier to be applied to the NavHost
 * @param navController The NavHostController managing navigation state (default: rememberNavController)
 */
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
        // Main video player screen
        composable<HomeRoute> { backStackEntry ->
            // Retrieve video selection result from SavedStateHandle (passed from VideoSearchContainer)
            var videoSelectionResult by remember { mutableStateOf<VideoSelectionResult?>(null) }

            LaunchedEffect(Unit) {
                backStackEntry.savedStateHandle
                    .getStateFlow<VideoSelectionResult?>("video_selection_result", null)
                    .collect { result ->
                        if (result != null) {
                            videoSelectionResult = result
                            // Clear the result from SavedStateHandle after consuming
                            backStackEntry.savedStateHandle.remove<VideoSelectionResult>("video_selection_result")
                        }
                    }
            }

            VideoContainer(
                onNavigateToSearch = { initialQuery ->
                    navController.navigate(VideoSearchRoute(initialQuery = initialQuery))
                },
                videoSelectionResult = videoSelectionResult,
                modifier = Modifier,
            )
        }

        // Video search bottom sheet
        bottomSheet<VideoSearchRoute> {
            VideoSearchContainer(
                onDismiss = { navController.popBackStack() },
                onVideoSelected = { result ->
                    // Pass result back to previous screen via SavedStateHandle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("video_selection_result", result)
                    navController.popBackStack()
                },
            )
        }
    }
}
