package org.example.project.testing.repository

import org.example.project.domain.model.SearchQuery
import org.example.project.domain.model.SearchResponse
import org.example.project.domain.model.VideoServiceType
import org.example.project.domain.repository.VideoSearchRepository

/**
 * テスト用VideoSearchRepository Fake実装。
 *
 * 検索結果のカスタマイズやエラーシミュレーションをサポート。
 */
class FakeVideoSearchRepository : VideoSearchRepository {
    var shouldReturnError: Boolean = false
    var errorToReturn: Throwable = RuntimeException("Fake error")
    var searchResponseToReturn: SearchResponse? = null
    var serviceSearchResponses: MutableMap<VideoServiceType, SearchResponse> = mutableMapOf()

    private val _searchVideosCalls = mutableListOf<SearchQuery>()
    private val _searchVideosByServiceCalls = mutableListOf<SearchByServiceCall>()

    val searchVideosCalls: List<SearchQuery> get() = _searchVideosCalls
    val searchVideosByServiceCalls: List<SearchByServiceCall> get() = _searchVideosByServiceCalls

    /**
     * searchVideosByServiceの呼び出し記録。
     */
    data class SearchByServiceCall(
        val searchQuery: SearchQuery,
        val serviceType: VideoServiceType,
    )

    override suspend fun searchVideos(searchQuery: SearchQuery): Result<SearchResponse> {
        _searchVideosCalls.add(searchQuery)

        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            searchResponseToReturn?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("No search response configured"))
        }
    }

    override suspend fun searchVideosByService(
        searchQuery: SearchQuery,
        serviceType: VideoServiceType,
    ): Result<SearchResponse> {
        _searchVideosByServiceCalls.add(SearchByServiceCall(searchQuery, serviceType))

        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            serviceSearchResponses[serviceType]?.let { Result.success(it) }
                ?: searchResponseToReturn?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("No search response configured"))
        }
    }

    /** テスト間で状態をリセット */
    fun reset() {
        shouldReturnError = false
        errorToReturn = RuntimeException("Fake error")
        searchResponseToReturn = null
        serviceSearchResponses.clear()
        _searchVideosCalls.clear()
        _searchVideosByServiceCalls.clear()
    }
}
