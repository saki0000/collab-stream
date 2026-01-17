package org.example.project.core.di

import org.example.project.data.local.DatabaseBuilder
import org.koin.dsl.module

actual fun platformModule() = module {
    // Database builder (iOS)
    single<DatabaseBuilder> {
        DatabaseBuilder()
    }
}
