package org.example.project.di

import org.example.project.video.VideoViewModel
import org.example.project.video.player.PlayerStateManager
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
        VideoViewModel(
            videoSyncUseCase = get(),
        )
    }
}
