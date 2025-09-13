# CollabStream システムアーキテクチャ

## 概要

Android Architecture（Domain層オプショナル）を採用した動画同期サービスの全体設計。

### アーキテクチャ採用理由
- **適切な複雑さ**: 現状はDomain層を必要としないシンプルなロジック
- **過剰設計回避**: URL パラメータ変更による簡単な同期処理
- **Google準拠**: Android公式推奨アーキテクチャ
- **柔軟な拡張**: 複雑化時にDomain層を追加可能

## システム構造

### レイヤー構成
```
┌─────────────────────────────────────┐
│           UI Layer                 │ ← MVI + Compose
│    (ViewModels, UI Components)     │
├─────────────────────────────────────┤
│        (Domain Layer)              │ ← Optional
│     (UseCases, Entities)           │   必要時に追加
├─────────────────────────────────────┤
│          Data Layer                │ ← Repository + API
│  (Repositories, DataSources, API)  │
└─────────────────────────────────────┘
```

### 依存関係
```
UI Layer → (Domain Layer) → Data Layer
※ 現状は UI Layer → Data Layer の直接接続
※ Android Architecture は一方向の依存関係
```

## UI Layer (プレゼンテーション層)

### ViewModels
```kotlin
class SyncSessionViewModel(
    private val streamRepository: StreamRepository
    // 将来: private val synchronizeUseCase: SynchronizeUseCase
) {
    // 現状: 直接Repository呼び出し
    fun synchronizeStreams(primary: String, secondary: String, timestamp: Long) {
        // URL パラメータ変更による同期
    }
    
    suspend fun loadStreamMetadata(url: String): StreamMetadata {
        return streamRepository.getMetadata(url)
    }
}
```

## (Domain Layer) - オプショナル

### 将来追加時の構造
```kotlin
// UseCases - ビジネスロジック
interface SynchronizeStreamsUseCase {
    suspend fun execute(primary: String, secondary: String, timestamp: Long): SyncResult
}

interface LoadStreamMetadataUseCase {
    suspend fun execute(url: String): StreamMetadata
}

// Entities - ビジネスエンティティ
data class StreamSync(
    val primaryUrl: String,
    val secondaryUrl: String, 
    val timestamp: Long
)

data class StreamMetadata(
    val id: String,
    val title: String,
    val duration: Long,
    val platform: StreamPlatform
)

enum class StreamPlatform { YouTube, Twitch }

// Repository Interfaces - Domain層が定義 (Data層が実装)
interface StreamRepository {
    suspend fun getMetadata(url: String): StreamMetadata
    suspend fun synchronizeUrls(primary: String, secondary: String, timestamp: Long): Pair<String, String>
}
```

## Data Layer (外部接続)

### DataSource 抽象化
```kotlin
interface StreamDataSource {
    suspend fun fetchMetadata(id: String): StreamMetadata
    suspend fun validateId(id: String): Boolean
}

// プラットフォーム固有実装
class YoutubeDataSource : StreamDataSource
class TwitchDataSource : StreamDataSource
```

### Repository 実装

```kotlin
// 現状: Data Layer が Repository Interface を定義・実装
interface StreamRepository {
    suspend fun getMetadata(url: String): StreamMetadata
    suspend fun synchronizeUrls(primary: String, secondary: String, timestamp: Long): Pair<String, String>
}

// Repository Implementation - Data Layer
class StreamRepositoryImpl(
    private val youtubeDataSource: YoutubeDataSource,
    private val twitchDataSource: TwitchDataSource
) : StreamRepository {
    
    override suspend fun getMetadata(url: String): StreamMetadata {
        return when (detectPlatform(url)) {
            StreamPlatform.YouTube -> youtubeDataSource.fetchMetadata(extractId(url))
            StreamPlatform.Twitch -> twitchDataSource.fetchMetadata(extractId(url))
        }
    }
    
    // 現状: Repository でビジネスロジック実装
    // 将来: Domain Layer 追加時はここはデータアクセスのみ
    override suspend fun synchronizeUrls(primary: String, secondary: String, timestamp: Long): Pair<String, String> {
        return Pair(
            addTimestamp(primary, timestamp),
            addTimestamp(secondary, timestamp)
        )
    }
    
    private fun addTimestamp(url: String, timestamp: Long): String = "$url&t=${timestamp}s"
}
```

### Domain Layer 追加時の変更
```kotlin
// Domain Layer が Repository Interface を定義
interface StreamRepository {
    suspend fun getMetadata(url: String): StreamMetadata
    // ビジネスロジックは UseCase に移動
}

// Data Layer は Domain Layer の interface を実装
class StreamRepositoryImpl(...) : StreamRepository {
    // データアクセスのみ実装
}
```

## API統合戦略

### セキュリティ段階的アプローチ

#### Phase 1: 環境変数 (開発初期)
```kotlin
object ApiConfig {
    val youtubeApiKey = BuildConfig.YOUTUBE_API_KEY
    val twitchClientId = BuildConfig.TWITCH_CLIENT_ID
    
    // レート制限
    val rateLimiter = RateLimiter.create(10.0)
}
```

#### Phase 2: プロキシサーバー (本格運用)
```kotlin
interface ApiService {
    suspend fun getYoutubeMetadata(videoId: String): YoutubeMetadata
    suspend fun getTwitchMetadata(videoId: String): TwitchMetadata
}

// 設定切り替え
val apiService: ApiService = if (BuildConfig.USE_PROXY) {
    ProxyApiService() // サーバー経由
} else {
    DirectApiService() // 直接API
}
```


## マルチプラットフォーム対応

### expect/actual 実装
```kotlin
// Common
expect class PlatformStreamPlayer {
    fun loadUrl(url: String)
    fun getCurrentPosition(): Long
}

// Android actual
actual class PlatformStreamPlayer {
    // Android固有実装
}

// iOS actual  
actual class PlatformStreamPlayer {
    // iOS固有実装
}

// Web actual
actual class PlatformStreamPlayer {
    // Web固有実装
}
```

## 将来拡張設計

### アカウント機能追加時
```kotlin
// Data Layer拡張
interface UserRepository {
    suspend fun authenticate(method: AuthMethod): User
    suspend fun savePreferences(user: User, prefs: UserPreferences)
}

class UserRepositoryImpl(
    private val authDataSource: AuthDataSource,
    private val preferencesDataSource: PreferencesDataSource
) : UserRepository
```

### 履歴機能追加時  
```kotlin
// Data Layer拡張
interface HistoryRepository {
    suspend fun saveSession(session: SyncSession)
    suspend fun getRecentSessions(): List<SyncSession>
}

// UI Layer拡張
class HomeViewModel(
    private val historyRepository: HistoryRepository
) {
    // 履歴表示ロジック
}
```

## ファイル構造

```
shared/src/commonMain/kotlin/
├── data/
│   ├── repositories/
│   ├── datasources/
│   ├── api/
│   └── models/
└── ui/
    ├── models/
    └── mappers/

composeApp/src/commonMain/kotlin/
├── ui/ (Component設計)
└── viewmodels/
```

## 品質保証

### テスト戦略
- **Domain Layer**: 純粋な単体テスト
- **Data Layer**: Repository + DataSource 結合テスト
- **Presentation Layer**: ViewModel + UI テスト

### エラーハンドリング
- **API制限**: レート制限 + リトライロジック
- **ネットワークエラー**: 指数バックオフ
- **無効URL**: バリデーション + ユーザーフィードバック

## まとめ

Clean Architecture により、シンプルな同期機能から始めて、将来のアカウント・履歴機能まで自然に拡張可能な設計を実現。段階的なセキュリティ対応で開発効率と安全性を両立。