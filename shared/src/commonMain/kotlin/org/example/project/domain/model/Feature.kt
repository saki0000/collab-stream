package org.example.project.domain.model

import kotlinx.serialization.Serializable

/**
 * Pro限定機能の識別子。
 *
 * Phase 0では空（プレースホルダー）。
 * 機能決定時にエントリを追加する。
 *
 * 使用例:
 * ```
 * UNLIMITED_CHANNELS(SubscriptionTier.PRO),
 * ```
 *
 * @property requiredTier この機能を利用するために必要なプラン種別
 */
@Serializable
enum class Feature(val requiredTier: SubscriptionTier)
