package org.example.project.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class SearchResult
@OptIn(ExperimentalTime::class)
constructor(
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelTitle: String,
    val publishedAt: Instant,
    val isLiveBroadcast: Boolean,
)

data class SearchQuery
@OptIn(ExperimentalTime::class)
constructor(
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

enum class SearchEventType(val value: String) {
    COMPLETED("completed"),
    LIVE("live"),
    UPCOMING("upcoming"),
    ANY("any"),
}

enum class SearchOrder(val value: String) {
    VIEW_COUNT("viewCount"),
    DATE("date"),
    RELEVANCE("relevance"),
    RATING("rating"),
}

data class SearchResponse(
    val results: List<SearchResult>,
    val nextPageToken: String? = null,
    val totalResults: Int,
    val hasMoreResults: Boolean = nextPageToken != null,
    val servicePageTokens: Map<VideoServiceType, String?> = emptyMap(),
)
