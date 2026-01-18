package org.example.project.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Spacing

/**
 * Home Screen (Stateless)
 * Initial screen with options to start searching for main streamer
 */
@Composable
fun HomeScreen(
    onSearchMainStreamer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
            ) {
                // App icon/logo
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.icon4xl),
                    tint = MaterialTheme.colorScheme.primary,
                )

                // Title
                Text(
                    text = "CollabStream",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = "Watch multiple streams in sync",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Main CTA: Search Main Streamer
                Button(
                    onClick = onSearchMainStreamer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.size(Spacing.sm))
                    Text("Search Main Streamer")
                }

                // TODO: Add "View Saved Groups" button when group save feature is implemented
                // OutlinedButton(
                //     onClick = onViewSavedGroups,
                //     modifier = Modifier.fillMaxWidth()
                // ) {
                //     Icon(imageVector = Icons.Default.Group, contentDescription = null)
                //     Spacer(modifier = Modifier.size(8.dp))
                //     Text("View Saved Groups")
                // }
            }
        }
    }
}
