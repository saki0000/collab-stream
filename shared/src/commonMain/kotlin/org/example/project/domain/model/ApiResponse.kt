package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * 共通APIレスポンスDTO
 *
 * サーバーとクライアント間のAPI通信で使用する統一されたレスポンス形式。
 * 成功/失敗の状態を型安全に表現する。
 *
 * @param T レスポンスデータの型
 */
@Serializable
sealed class ApiResponse<out T> {

    /**
     * API呼び出し成功時のレスポンス
     *
     * @property data レスポンスデータ
     */
    @Serializable
    data class Success<T>(
        val data: T,
    ) : ApiResponse<T>()

    /**
     * API呼び出し失敗時のレスポンス
     *
     * @property message エラーメッセージ
     * @property code エラーコード（HTTPステータスコード等）
     */
    @Serializable
    data class Error(
        val message: String,
        val code: Int,
    ) : ApiResponse<Nothing>()
}
