package org.example.project.service

import org.example.project.domain.model.SubscriptionStatus

/**
 * サブスクリプション検証サービス
 *
 * RevenueCat REST API v1 と連携して、デバイスIDに紐づく
 * サブスクリプション状態を取得する。
 *
 * Epic: サブスクリプション検証
 * US-5: サーバーサイドサブスクリプション検証API
 */
interface SubscriptionService {

    /**
     * デバイスIDに紐づくサブスクリプション状態を取得
     *
     * @param deviceId デバイスID（UUID v4形式）。RevenueCat の appUserId として使用。
     * @return サブスクリプション状態。未登録ユーザーの場合は FREE プランを返す。
     * @throws org.example.project.plugins.ServiceUnavailableException RevenueCat APIキー未設定時（503）
     * @throws org.example.project.plugins.ExternalApiException RevenueCat API呼び出し失敗時（502）
     */
    suspend fun getSubscriptionStatus(deviceId: String): SubscriptionStatus
}
