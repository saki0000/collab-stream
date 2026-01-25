package org.example.project.feature.streamer_search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.core.theme.AppShapes
import org.example.project.core.theme.Dimensions
import org.example.project.core.theme.Elevation
import org.example.project.core.theme.Spacing
import org.example.project.domain.model.SearchResult

/**
 * Component (Reusable) - Individual search result item
 * Displays video thumbnail, title, channel, and metadata
 * Supports selection state for multi-select in sub search mode
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SearchResultItem(
    result: SearchResult,
    isSelected: Boolean,
    isSubSearchMode: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.low),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isSubSearchMode) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Selection checkbox (only in sub search mode)
            if (isSubSearchMode) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(Dimensions.iconLg),
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimensions.iconLg),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(Dimensions.iconLg)
                                .border(
                                    width = Spacing.xxs,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
            }

            // Thumbnail
            AsyncImage(
                model = result.thumbnailUrl,
                contentDescription = result.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = Dimensions.thumbnailMdWidth, height = Dimensions.thumbnailMdHeight)
                    .clip(AppShapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                // Title
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                // Channel
                Text(
                    text = result.channelTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Published date
                Text(
                    text = formatPublishedDate(result.publishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Live broadcast indicator
                if (result.isLiveBroadcast) {
                    Text(
                        text = "Live Stream Archive",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                AppShapes.small,
                            )
                            .padding(horizontal = 6.dp, vertical = Spacing.xxs),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun formatPublishedDate(publishedAt: Instant): String {
    val localDateTime = publishedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.month} ${localDateTime.day}, ${localDateTime.year}"
}
