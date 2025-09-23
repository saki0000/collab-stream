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

data class SearchQuery(
    val query: String,
    val maxResults: Int = 25,
    val pageToken: String? = null,
    val eventType: SearchEventType = SearchEventType.COMPLETED,
)

enum class SearchEventType(val value: String) {
    COMPLETED("completed"),
    LIVE("live"),
    UPCOMING("upcoming"),
    ANY("any"),
}

data class SearchResponse(
    val results: List<SearchResult>,
    val nextPageToken: String? = null,
    val totalResults: Int,
    val hasMoreResults: Boolean = nextPageToken != null,
)
