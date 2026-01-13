@file:OptIn(kotlin.time.ExperimentalTime::class)

package org.example.project.testing.data

import kotlin.time.Instant
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType

/**
 * SearchResult テストデータ生成ユーティリティ。
 */
object SearchResultTestData {
    val DEFAULT_PUBLISHED_AT: Instant = Instant.parse("2023-12-25T10:00:00Z")

    /**
     * 標準的なSearchResult を生成。
     */
    fun createSearchResult(
        videoId: String = "test-video-id",
        title: String = "Test Video",
        description: String = "Test Description",
        thumbnailUrl: String = "https://example.com/thumbnail.jpg",
        channelTitle: String = "Test Channel",
        publishedAt: Instant = DEFAULT_PUBLISHED_AT,
        isLiveBroadcast: Boolean = false,
        serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
    ): SearchResult =
        SearchResult(
            videoId = videoId,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
            channelTitle = channelTitle,
            publishedAt = publishedAt,
            isLiveBroadcast = isLiveBroadcast,
            serviceType = serviceType,
        )

    /**
     * 複数のSearchResult リストを生成。
     */
    fun createSearchResultList(
        count: Int = 5,
        serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
        baseId: String = "video",
    ): List<SearchResult> =
        (0 until count).map { index ->
            createSearchResult(
                videoId = "$baseId-$index",
                title = "Video $index",
                serviceType = serviceType,
            )
        }

    /**
     * 標準的なSearchResponse を生成。
     */
    fun createSearchResponse(
        results: List<SearchResult> = createSearchResultList(),
        nextPageToken: String? = null,
        totalResults: Int = results.size,
    ): SearchResponse =
        SearchResponse(
            results = results,
            nextPageToken = nextPageToken,
            totalResults = totalResults,
            hasMoreResults = nextPageToken != null,
        )

    /**
     * ページネーション付きSearchResponse を生成。
     */
    fun createSearchResponseWithPagination(
        results: List<SearchResult> = createSearchResultList(),
        nextPageToken: String = "next-page-token",
        totalResults: Int = 100,
    ): SearchResponse =
        SearchResponse(
            results = results,
            nextPageToken = nextPageToken,
            totalResults = totalResults,
            hasMoreResults = true,
        )

    /**
     * 空のSearchResponse を生成。
     */
    fun createEmptySearchResponse(): SearchResponse =
        SearchResponse(
            results = emptyList(),
            nextPageToken = null,
            totalResults = 0,
            hasMoreResults = false,
        )
}
