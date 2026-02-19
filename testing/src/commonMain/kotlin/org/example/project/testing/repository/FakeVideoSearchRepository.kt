package org.example.project.testing.repository

import org.example.project.domain.model.ChannelInfo
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
    var channelSearchResultToReturn: List<ChannelInfo> = emptyList()

    private val _searchVideosCalls = mutableListOf<SearchQuery>()
    private val _searchVideosByServiceCalls = mutableListOf<SearchByServiceCall>()
    private val _searchChannelsCalls = mutableListOf<SearchChannelsCall>()

    val searchVideosCalls: List<SearchQuery> get() = _searchVideosCalls
    val searchVideosByServiceCalls: List<SearchByServiceCall> get() = _searchVideosByServiceCalls
    val searchChannelsCalls: List<SearchChannelsCall> get() = _searchChannelsCalls

    /**
     * searchVideosByServiceの呼び出し記録。
     */
    data class SearchByServiceCall(
        val searchQuery: SearchQuery,
        val serviceType: VideoServiceType,
    )

    /**
     * searchChannelsの呼び出し記録。
     */
    data class SearchChannelsCall(
        val query: String,
        val serviceType: VideoServiceType,
        val maxResults: Int,
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

    override suspend fun searchChannels(
        query: String,
        serviceType: VideoServiceType,
        maxResults: Int,
    ): Result<List<ChannelInfo>> {
        _searchChannelsCalls.add(SearchChannelsCall(query, serviceType, maxResults))

        return if (shouldReturnError) {
            Result.failure(errorToReturn)
        } else {
            Result.success(channelSearchResultToReturn)
        }
    }

    /** テスト間で状態をリセット */
    fun reset() {
        shouldReturnError = false
        errorToReturn = RuntimeException("Fake error")
        searchResponseToReturn = null
        serviceSearchResponses.clear()
        channelSearchResultToReturn = emptyList()
        _searchVideosCalls.clear()
        _searchVideosByServiceCalls.clear()
        _searchChannelsCalls.clear()
    }
}
