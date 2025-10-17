package org.example.project.feature.streamer_search

import kotlinx.datetime.LocalDate
import org.example.project.domain.model.ChannelInfo
import org.example.project.domain.model.SearchResult
import org.example.project.domain.model.VideoServiceType
import org.example.project.feature.video_search.SearchMode

/**
 * User intents for streamer search
 */
sealed interface StreamerSearchIntent {
    data class UpdateInputText(val text: String) : StreamerSearchIntent
    data object ExecuteSearch : StreamerSearchIntent
    data class SelectSearchResult(val result: SearchResult) : StreamerSearchIntent
    data object LoadMoreSearchResults : StreamerSearchIntent
    data object ClearSearchError : StreamerSearchIntent
    data class ChangeSelectedDate(val date: LocalDate) : StreamerSearchIntent
    data class ChangeSearchMode(val mode: SearchMode) : StreamerSearchIntent
    data class SelectService(val service: VideoServiceType) : StreamerSearchIntent
    data class SearchChannels(val query: String) : StreamerSearchIntent
    data class SelectChannel(val channel: ChannelInfo) : StreamerSearchIntent
}
