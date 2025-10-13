package org.example.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.example.project.domain.model.VideoServiceType
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
 * Uses type-safe navigation with kotlinx.serialization:
 * - Forward: Navigation Arguments (VideoSearchRoute with initialQuery)
 * - Backward: Navigation Arguments (HomeRoute with selectedVideoId and selectedServiceType)
 *
 * Navigation result handling is centralized here to maintain proper separation of concerns.
 * Feature-level components (VideoContainer) remain unaware of navigation implementation details.
 *
 * Video selection results are passed as primitive types (String) to avoid NavType issues with custom classes.
 * The VideoSelectionResult is reconstructed from primitives in the navigation layer.
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
        startDestination = HomeRoute(),
        modifier = modifier,
    ) {
        // Main video player screen
        composable<HomeRoute> { backStackEntry ->
            // Retrieve video selection parameters from navigation arguments
            val route = backStackEntry.toRoute<HomeRoute>()

            // Reconstruct VideoSelectionResult from primitive types if both are present
            val videoSelectionResult = if (route.selectedVideoId != null && route.selectedServiceType != null) {
                try {
                    VideoSelectionResult(
                        videoId = route.selectedVideoId,
                        serviceType = VideoServiceType.valueOf(route.selectedServiceType),
                    )
                } catch (e: IllegalArgumentException) {
                    // Invalid service type, ignore
                    null
                }
            } else {
                null
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
                    // Navigate back to HomeRoute with individual primitive parameters
                    navController.navigate(
                        HomeRoute(
                            selectedVideoId = result.videoId,
                            selectedServiceType = result.serviceType.name,
                        ),
                    ) {
                        // Pop up to the original HomeRoute and replace it
                        popUpTo<HomeRoute> {
                            inclusive = true
                        }
                    }
                },
            )
        }
    }
}
