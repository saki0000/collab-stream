package org.example.project.di

import org.koin.dsl.module

// Platform-specific implementation removed - now using unified PlaybackPositionProviderImpl
// with PlayerStateManager from commonMain

actual fun platformModule(): org.koin.core.module.Module = module {
    // Web-specific dependencies can be added here if needed
    // PlaybackPositionProvider is now managed by the common appModule using PlayerStateManager
}
