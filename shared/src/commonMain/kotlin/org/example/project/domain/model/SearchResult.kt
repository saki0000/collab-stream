@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.InstantComponentSerializer

@Serializable
data class SearchResult(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    @Serializable(with = InstantComponentSerializer::class)
    val publishedAt: Instant,
    val isLiveBroadcast: Boolean,
    val serviceType: VideoServiceType,
)

@Serializable
data class SearchQuery(
    val query: String,
    val maxResults: Int = 25,
    val pageToken: String? = null,
    val eventType: SearchEventType = SearchEventType.COMPLETED,
    val publishedAfter: Instant? = null,
    val publishedBefore: Instant? = null,
    val order: SearchOrder = SearchOrder.VIEW_COUNT,
    val channelId: String? = null,
    val targetServices: Set<VideoServiceType> = setOf(VideoServiceType.YOUTUBE, VideoServiceType.TWITCH),
)

@Serializable
enum class SearchEventType(val value: String) {
    COMPLETED("completed"),
    LIVE("live"),
    UPCOMING("upcoming"),
    ANY("any"),
}

@Serializable
enum class SearchOrder(val value: String) {
    VIEW_COUNT("viewCount"),
    DATE("date"),
    RELEVANCE("relevance"),
    RATING("rating"),
}

@Serializable
data class SearchResponse(
    val results: List<SearchResult>,
    val nextPageToken: String? = null,
    val totalResults: Int,
    val hasMoreResults: Boolean = nextPageToken != null,
    val servicePageTokens: Map<VideoServiceType, String?> = emptyMap(),
)
