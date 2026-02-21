@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.core.di

import org.example.project.feature.archive_home.ArchiveHomeViewModel
import org.example.project.feature.home.HomeViewModel
import org.example.project.feature.streamer_search.StreamerSearchViewModel
import org.example.project.feature.subscription.SubscriptionViewModel
import org.example.project.feature.timeline_sync.TimelineSyncViewModel
import org.example.project.feature.video_playback.VideoViewModel
import org.example.project.feature.video_playback.player.PlayerStateManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI module for the application layer (composeApp) containing presentation components.
 * This module provides dependencies for:
 * - ViewModels
 * - Controllers
 * - UI-related services
 * Configuration is now handled entirely in SharedModule - much simpler!
 */
val appModule = module {

    // Player state management
    single<PlayerStateManager> { PlayerStateManager() }

    // ViewModels
    viewModel {
        HomeViewModel()
    }

    viewModel {
        StreamerSearchViewModel(
            videoSearchUseCase = get(),
            channelSearchUseCase = get(),
            savedStateHandle = get(),
        )
    }

    viewModel {
        VideoViewModel(
            videoSyncUseCase = get(),
        )
    }

    viewModel {
        TimelineSyncViewModel(
            timelineSyncRepository = get(),
            channelSearchUseCase = get(),
            channelFollowRepository = get(),
            commentRepository = get(),
            syncHistoryRepository = get(),
        )
    }

    viewModel {
        ArchiveHomeViewModel(
            timelineSyncRepository = get(),
            channelFollowRepository = get(),
            channelSearchUseCase = get(),
        )
    }

    viewModel {
        SubscriptionViewModel(
            subscriptionRepository = get(),
        )
    }
}
