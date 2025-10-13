package org.example.project.data.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlin.time.ExperimentalTime
import org.exampl.project.BuildKonfig
import org.example.project.data.model.TwitchSearchResponse
import org.example.project.data.model.TwitchUserResponse
import org.example.project.domain.model.SearchEventType
import org.example.project.domain.model.SearchQuery

class TwitchSearchDataSourceImpl(
    private val httpClient: HttpClient,
) : TwitchSearchDataSource {

    companion object {
        private const val TWITCH_API_BASE_URL = "https://api.twitch.tv/helix"
        private const val TWITCH_USERS_ENDPOINT = "$TWITCH_API_BASE_URL/search/channels"
        private const val TWITCH_VIDEOS_ENDPOINT = "$TWITCH_API_BASE_URL/videos"
    }

    /**
     * Get Twitch user ID from login name.
     * Calls the /users endpoint to convert login name to user ID.
     *
     * @param login The user's login name (e.g., "shroud")
     * @return The user's ID (e.g., "37402112"), or null if not found
     */
    private suspend fun getUserIdByLogin(query: String): String? {
        return try {
            val response = httpClient.get(TWITCH_USERS_ENDPOINT) {
                header("Client-ID", BuildKonfig.TWITCH_CLIENT_ID)
                header("Authorization", "Bearer ${BuildKonfig.TWITCH_API_KEY}")
                parameter("query", query)
            }

            val userResponse: TwitchUserResponse = response.body()

            // Check for API error response
            if (!userResponse.error.isNullOrBlank()) {
                return null
            }

            // Return the first user's ID, or null if no users found
            userResponse.data.firstOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun searchVideos(searchQuery: SearchQuery): Result<TwitchSearchResponse> {
        return try {
            if (BuildKonfig.TWITCH_CLIENT_ID.isBlank() || BuildKonfig.TWITCH_API_KEY.isBlank()) {
                return Result.failure(IllegalStateException("Twitch API credentials are not configured"))
            }

            // Note: Twitch Helix API doesn't have a direct video search endpoint like YouTube.
            // The /videos endpoint requires user_id or game_id as a filter.
            // We first need to get the user_id from the user's login name using /users endpoint.

            // Try to extract a user login from the query if it looks like a channel name
            val userLogin = searchQuery.query

            if (userLogin.isBlank()) {
                // Twitch doesn't support free-text video search without a user_id
                return Result.success(
                    TwitchSearchResponse(
                        data = emptyList(),
                        pagination = null,
                    ),
                )
            }

            // Step 1: Get user_id from login name using /users endpoint
            val userId = getUserIdByLogin(userLogin)

            if (userId == null) {
                // User not found, return empty results
                return Result.success(
                    TwitchSearchResponse(
                        data = emptyList(),
                        pagination = null,
                    ),
                )
            }

            // Step 2: Get videos using the user_id
            val response = httpClient.get(TWITCH_VIDEOS_ENDPOINT) {
                header("Client-ID", BuildKonfig.TWITCH_CLIENT_ID)
                header("Authorization", "Bearer ${BuildKonfig.TWITCH_API_KEY}")

                // Add query parameters - use user_id instead of user_login
                parameter("user_id", userId)
                parameter("first", searchQuery.maxResults.coerceIn(1, 100))

                // Map SearchEventType to Twitch video type
                when (searchQuery.eventType) {
                    SearchEventType.COMPLETED -> parameter("type", "archive")
                    SearchEventType.LIVE -> {
                        // Twitch /videos doesn't return live streams, only VODs
                        // For live streams, we'd need to use /streams endpoint
                    }
                    else -> {
                        // Default to archive (completed streams)
                        parameter("type", "archive")
                    }
                }

                // Add pagination token if provided
                searchQuery.pageToken?.let { pageToken ->
                    parameter("after", pageToken)
                }

                // Note: Twitch API doesn't have direct equivalents for YouTube's publishedAfter/Before
                // These would need to be filtered client-side if needed
            }

            val apiResponse: TwitchSearchResponse = response.body()

            // Check for API error response
            if (!apiResponse.error.isNullOrBlank()) {
                return Result.failure(
                    RuntimeException("Twitch API error: ${apiResponse.error} - ${apiResponse.message ?: "Unknown error"}"),
                )
            }

            Result.success(apiResponse)
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to search Twitch videos for query '${searchQuery.query}': ${e.message}", e),
            )
        }
    }
}
