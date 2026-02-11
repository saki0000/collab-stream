package org.example.project.domain.model

/**
 * 外部アプリ起動用のDeepLink情報。
 *
 * プラットフォーム固有のDeepLink URIとWebフォールバックURLを保持する。
 * DeepLink URI失敗時にフォールバックURLで外部ブラウザを開く。
 *
 * Epic: Timeline Sync (EPIC-002)
 * Story: US-4 (外部アプリ連携)
 */
data class DeepLinkInfo(
    /**
     * プラットフォーム固有のDeepLink URI。
     * 例: youtube://watch?v=xxx&t=120, twitch://video/xxx?t=120s
     */
    val deepLinkUri: String,

    /**
     * WebブラウザフォールバックURL。
     * DeepLink失敗時に使用する。
     * 例: https://www.youtube.com/watch?v=xxx&t=120s
     */
    val fallbackUrl: String,
)

/**
 * SyncChannelからDeepLinkInfoを生成する。
 *
 * @return DeepLinkInfo、またはストリーム未選択・再生位置未計算の場合はnull
 */
@kotlin.time.ExperimentalTime
fun SyncChannel.toDeepLinkInfo(): DeepLinkInfo? {
    val stream = selectedStream ?: return null
    val seekSeconds = targetSeekPosition?.toInt()?.coerceAtLeast(0) ?: return null
    val videoId = stream.id

    return when (serviceType) {
        VideoServiceType.YOUTUBE -> DeepLinkInfo(
            deepLinkUri = "youtube://watch?v=$videoId&t=$seekSeconds",
            fallbackUrl = "https://www.youtube.com/watch?v=$videoId&t=${seekSeconds}s",
        )
        VideoServiceType.TWITCH -> DeepLinkInfo(
            deepLinkUri = "twitch://video/$videoId?t=${seekSeconds}s",
            fallbackUrl = "https://www.twitch.tv/videos/$videoId?t=${seekSeconds}s",
        )
    }
}
