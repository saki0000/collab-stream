package org.example.project.di

import org.koin.dsl.module

actual fun platformModule() = module {
    // Android-specific dependencies can be added here if needed
    // PlaybackPositionProvider is now managed by the common appModule using PlayerStateManager
}
