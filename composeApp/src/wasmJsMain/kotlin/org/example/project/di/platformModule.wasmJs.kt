package org.example.project.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // WASM-specific dependencies can be added here if needed
    // PlaybackPositionProvider is now managed by the common appModule using PlayerStateManager
}
