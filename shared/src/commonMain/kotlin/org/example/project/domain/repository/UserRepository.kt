package org.example.project.domain.repository

/**
 * ユーザー識別のRepository。
 *
 * 匿名認証（デバイスIDベース、UUID v4）によるユーザー識別を担当する。
 * デバイスIDの生成と永続化を提供する。
 *
 * Epic: サブスクリプション基盤
 * Shared across: US-2 (匿名認証), US-3 (RevenueCat統合)
 */
interface UserRepository {

    /**
     * デバイスIDを取得する。
     *
     * 未生成の場合は新しいUUID v4を生成・永続化して返す。
     *
     * @return デバイスID（UUID v4形式の文字列）
     */
    suspend fun getDeviceId(): String

    /**
     * デバイスIDが既に生成・保存済みかどうかを確認する。
     *
     * @return 保存済みならtrue
     */
    suspend fun hasDeviceId(): Boolean
}
