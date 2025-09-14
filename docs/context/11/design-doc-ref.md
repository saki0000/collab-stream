# Design Doc Reference - Issue #11

## 📖 Design Doc情報

**Location**: Issue body内に完全なDesign Docが含まれている
**Status**: Interface設計完了
**Approach**: クライアント直接API呼び出しアプローチ

## 🏗️ Interface設計詳細

### Domain Layer Interfaces

```kotlin
interface VideoSyncRepository {
    // YouTube API連携責務
    suspend fun getVideoDetails(videoId: String): Result<YouTubeVideoDetails>
}

interface VideoSyncUseCase {
    // ビジネスロジック責務
    suspend fun syncVideoToAbsoluteTime(
        videoId: String,
        currentPlaybackSeconds: Float
    ): Result<VideoSyncInfo>
}

interface PlaybackPositionProvider {
    // プラットフォーム固有の再生位置取得責務
    suspend fun getCurrentPlaybackPosition(): Result<Float>
}
```

### Presentation Layer Interfaces

```kotlin
interface VideoSyncController {
    // UI操作・状態管理責務
    suspend fun handleSyncButtonClick()
    val syncState: VideoSyncUiState
}
```

### Data Models

```kotlin
data class VideoSyncInfo(
    val videoId: String,
    val playbackSeconds: Float,
    val streamStartTime: Instant,
    val absoluteTime: Instant
)

data class YouTubeVideoDetails(
    val id: String,
    val liveStreamingDetails: LiveStreamingDetails?
)

data class LiveStreamingDetails(
    val actualStartTime: Instant
)
```

## 📊 責務マトリックス

| レイヤー | クラス/Interface | 責務 |
|---------|------------------|------|
| Domain | VideoSyncUseCase | 時刻計算ロジック、同期処理の調整 |
| Domain | VideoSyncRepository | YouTube Data API呼び出し |
| Presentation | PlaybackPositionProvider | プラットフォーム固有の再生位置取得 |
| Presentation | VideoSyncController | UI状態管理、ユーザーアクション処理 |

## 🔗 既存コード参照

- VideoPlayerView (Android): `composeApp/src/androidMain/kotlin/org/example/project/video/VideoPlayerView.android.kt:23`
- VideoPlayerView (iOS): `composeApp/src/iosMain/kotlin/org/example/project/video/VideoPlayerView.ios.kt:14`

## 🎯 実装指針

1. **shared層優先**: ビジネスロジックは共通実装
2. **expect/actual活用**: プラットフォーム固有の再生位置取得
3. **既存パターン踏襲**: VideoPlayerViewの拡張として実装
4. **エラーハンドリング**: Result型で統一的なエラー管理