package org.example.project.di

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication

/**
 * Initializes Koin DI container with all required modules.
 * This should be called once at the application root level.
 */
@Composable
fun KoinInitializer(content: @Composable () -> Unit) {
    KoinApplication(application = {
        modules(
            sharedModule,
            appModule,
            platformModule(),
        )
    }) {
        content()
    }
}
