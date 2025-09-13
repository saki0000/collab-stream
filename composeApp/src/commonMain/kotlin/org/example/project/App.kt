package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import collabstream.composeapp.generated.resources.Res
import collabstream.composeapp.generated.resources.compose_multiplatform
import org.example.project.video.ui.VideoContainer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun app() {
    MaterialTheme {
        var showVideoDemo by remember { mutableStateOf(false) }
        var showOriginalContent by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Demo selection buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        showVideoDemo = !showVideoDemo
                        showOriginalContent = false
                    }
                ) {
                    Text(if (showVideoDemo) "Hide YouTube Demo" else "Show YouTube Demo")
                }

                Button(
                    onClick = {
                        showOriginalContent = !showOriginalContent
                        showVideoDemo = false
                    }
                ) {
                    Text(if (showOriginalContent) "Hide Original Demo" else "Show Original Demo")
                }
            }

            // YouTube Video Demo
            AnimatedVisibility(showVideoDemo) {
                VideoContainer(
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Original demo content
            AnimatedVisibility(showOriginalContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}
