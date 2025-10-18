package org.example.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.example.project.core.di.KoinInitializer
import org.example.project.core.navigation.AppNavGraph
import org.example.project.core.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    KoinInitializer {
        MaterialTheme {
            AppTheme {
                AppNavGraph(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
