package org.example.project.data.util

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import org.example.project.domain.model.ApiResponse

/**
 * ApiResponse のハンドリングユーティリティ
 *
 * サーバーAPIからの ApiResponse レスポンスをデシリアライズし、
 * 成功/失敗を Result 型に変換する。
 */
object ApiResponseHandler {

    /**
     * HTTP レスポンスを ApiResponse としてデシリアライズし、Result に変換する
     *
     * @param response HTTP レスポンス
     * @return 成功時は Result.success(data)、失敗時は Result.failure(exception)
     */
    suspend inline fun <reified T> handleResponse(response: HttpResponse): Result<T> {
        return try {
            if (response.status.isSuccess()) {
                // 2xx レスポンス: ApiResponse.Success としてデシリアライズ
                val apiResponse: ApiResponse.Success<T> = response.body()
                Result.success(apiResponse.data)
            } else {
                // 4xx / 5xx レスポンス: ApiResponse.Error としてデシリアライズ
                val errorResponse: ApiResponse.Error = response.body()
                Result.failure(
                    RuntimeException("API Error (${errorResponse.code}): ${errorResponse.message}"),
                )
            }
        } catch (e: Exception) {
            Result.failure(
                RuntimeException("Failed to parse API response: ${e.message}", e),
            )
        }
    }
}
