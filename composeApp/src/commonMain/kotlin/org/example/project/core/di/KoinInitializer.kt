package org.example.project.core.di

import androidx.compose.runtime.Composable
import org.example.project.di.databaseModule
import org.example.project.di.sharedModule
import org.koin.compose.KoinApplication

/**
 * Initializes Koin DI container with all required modules.
 * This should be called once at the application root level.
 */
@Composable
fun KoinInitializer(content: @Composable () -> Unit) {
    KoinApplication(application = {
        modules(
            // Platform-specific modules first (provides DatabaseBuilder)
            platformModule(),
            // Database module (uses DatabaseBuilder from platformModule)
            databaseModule,
            // Shared module
            sharedModule,
            // App module (ViewModels)
            appModule,
        )
    }) {
        content()
    }
}
