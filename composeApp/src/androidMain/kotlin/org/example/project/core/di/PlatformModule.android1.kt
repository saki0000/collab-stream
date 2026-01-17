package org.example.project.core.di

import org.example.project.data.local.DatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual fun platformModule() = module {
    // Database builder (Android requires Context)
    single<DatabaseBuilder> {
        DatabaseBuilder(androidContext())
    }
}
