# Design Document: YouTube再生時間取得機能

## 機能概要

> 全てのエンジニアが理解し、このドキュメントを読むべきかどうかを判断するための高レベルな要約です。3段落以内で簡潔にプロジェクトの内容を説明します。

埋め込みYouTube動画の現在の再生位置（秒）と配信開始時間を元に、実際の日時に変換して取得する機能を実装します。この機能は、複数の動画を時刻で同期視聴するための基盤機能として位置づけられています。

ユーザーが動画同期ボタンを押下した時点で、YouTube Player APIから現在の再生位置を取得し、YouTube Data API v3から配信開始時間を取得して、絶対的な日時を計算します。計算結果は一時的にメモリに保持され、将来の同期機能で活用される予定です。

対象プラットフォームはAndroidとiOSで、それぞれ既存のVideoPlayerViewの実装を拡張する形で機能を追加します。

## 実装背景

> このプロジェクトが必要とされる理由や問題点について記載します。また、このプロジェクトがどのように技術的戦略や製品戦略、チームの四半期目標に関連しているか、またはプロジェクトを評価する際に知っておくべきことを説明します。

### 課題・問題点
- YouTube動画の再生位置は相対的な秒数でしか取得できない
- 配信開始時間との関連付けができない
- 複数動画間での時刻同期ができない

### 戦略との関連
- **技術的戦略**: マルチプラットフォーム対応とKotlin Multiplatformの活用
- **製品戦略**: 動画同期機能の基盤として将来の機能拡張を支える
- **四半期目標**: 同期視聴機能のフェーズ1として位置づけ

## 影響・対象範囲

> このドキュメントがカバーする範囲について記載します。

### 対象システム・コンポーネント
- VideoPlayerView（Android/iOS）
- shared モジュール（ビジネスロジック）
- composeApp モジュール（UI）

### 対象ユーザー
- アプリのエンドユーザー

### 対象プラットフォーム
- Android ✅
- iOS ✅
- Web (WASM) ❌
- Server ❌

## ゴール

### ユーザーへの影響
- 複数の配信動画を時刻で同期して視聴できる準備ができる
- 特定の時刻の出来事を正確に記録・参照できる基盤が整う
- 時刻ベースでのブックマークや共有が可能になる基盤を提供

### 成功指標
- **機能利用率**: 同期ボタンの使用回数
- **精度**: 時刻計算の正確性（配信開始時間との誤差1秒以内）
- **レスポンス時間**: API取得から結果表示まで3秒以内

### トラッキング
- 同期ボタンクリックイベント
- API取得成功/失敗率
- レスポンス時間メトリクス

## 対象外項目

> 非目標は、解決しない問題について明確に記述します。これにより、関係者全員が同じ認識を持つことができます。

- 複数動画の同期機能そのもの（将来実装予定）
- データの永続化・キャッシュ機能
- Web（WASM）プラットフォーム対応
- リアルタイム監視（ボタン押下時のみ実装）
- 動画再生コントロール機能の追加
- 配信開始時間の手動編集機能

## Interface設計（必須）

> **必須フェーズ**: すべての機能実装においてInterface設計を行います。

### レイヤー別Interface定義

#### Domain Layer Interfaces

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

#### Presentation Layer Interfaces

```kotlin
interface VideoSyncController {
    // UI操作・状態管理責務
    suspend fun handleSyncButtonClick()
    val syncState: VideoSyncUiState
}
```

#### Data Models

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

### 責務マトリックス

| レイヤー | クラス/Interface | 責務 |
|---------|------------------|------|
| Domain | VideoSyncUseCase | 時刻計算ロジック、同期処理の調整 |
| Domain | VideoSyncRepository | YouTube Data API呼び出し |
| Presentation | PlaybackPositionProvider | プラットフォーム固有の再生位置取得 |
| Presentation | VideoSyncController | UI状態管理、ユーザーアクション処理 |

### 依存関係図

```
VideoSyncController → VideoSyncUseCase → VideoSyncRepository
                   ↘ PlaybackPositionProvider
```

## 設計アプローチ

> 選択された設計アプローチに基づいて、実装戦略と技術的方針を記載します。

### 選択した設計アプローチ
**A: クライアント直接API呼び出しアプローチ**

**選択理由**:
- シンプルな実装でレスポンス時間が早い
- サーバーサイドの実装が不要
- 初期実装として最適な複雑度

### Serena分析結果

> コードベース分析とアーキテクチャ設計の結果を記載します。

#### 既存コード分析
**類似機能の実装パターン**:
- VideoPlayerView: `composeApp/src/androidMain/kotlin/org/example/project/video/VideoPlayerView.android.kt:23` - YouTubePlayerを使用したプラットフォーム固有実装
- VideoPlayerView: `composeApp/src/iosMain/kotlin/org/example/project/video/VideoPlayerView.ios.kt:14` - WKWebViewとiframe APIを使用した実装

**使用フレームワーク・ライブラリ**:
- YouTube Android Player API: Android版での動画再生
- WKWebView: iOS版での動画再生
- Kotlin Multiplatform: 共通ロジックの実装

**アーキテクチャ一貫性**:
- expect/actual パターンでプラットフォーム固有実装を分離
- Compose Multiplatformによる共通UI実装

#### 設計最適化
**アーキテクチャ決定**:
- shared層でのビジネスロジック実装: プラットフォーム間での一貫性確保
- expect/actualパターンでの再生位置取得: 各プラットフォームの最適な実装を活用

**プラットフォーム考慮事項**:
- **Android**: YouTubePlayer.getCurrentTime()での再生位置取得
- **iOS**: JavaScript経由でのYouTube Player API呼び出し
- **Web**: 今回は対象外
- **Server**: 今回は対象外

**依存関係管理**:
- YouTube Data API v3クライアントライブラリの追加
- APIキーの設定管理

## 解決策 / 技術アーキテクチャ

### 全体概要

> 選択した設計アプローチとSerena分析結果に基づく具体的な解決策を説明します。

YouTube Player APIから現在の再生位置を取得し、YouTube Data API v3から配信開始時間を取得して、絶対的な日時を計算する機能を実装します。計算ロジックはshared層に配置し、プラットフォーム固有の再生位置取得はexpect/actualパターンで実装します。

### システムコンテキスト図

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   VideoSyncUI   │───▶│  VideoSyncUseCase │───▶│YouTube Data API │
│   (ComposeApp)  │    │     (Shared)     │    │       v3        │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                        │
         ▼                        │
┌─────────────────┐              │
│PlaybackPosition │              │
│   Provider      │              │
│ (Platform)      │◄─────────────┘
└─────────────────┘
         │
         ▼
┌─────────────────┐
│ YouTube Player  │
│      API        │
└─────────────────┘
```

### 実装戦略

#### 開発フェーズ
**Phase 1: 基盤構築**
- Interface定義とデータモデル実装
- YouTube Data API v3クライアント設定
- APIキー設定管理

**Phase 2: コア機能実装**
- VideoSyncUseCaseの時刻計算ロジック実装
- VideoSyncRepositoryのAPI呼び出し実装
- PlaybackPositionProviderのプラットフォーム固有実装

**Phase 3: UI統合**
- 同期ボタンの追加
- VideoSyncControllerの実装
- エラーハンドリングとUI表示

#### モジュール設計
**Shared Module (共通ロジック)**
- VideoSyncUseCase: 時刻計算ロジック
- VideoSyncRepository: API呼び出し
- データモデル定義

**ComposeApp Module (UI)**
- VideoSyncController: UI状態管理
- PlaybackPositionProvider: プラットフォーム固有実装
- 同期ボタンUI

### API

#### 外部API使用
```http
# YouTube Data API v3
GET https://www.googleapis.com/youtube/v3/videos
?part=liveStreamingDetails
&id={videoId}
&key={apiKey}
```

#### データ形式
```json
{
  "items": [{
    "id": "videoId",
    "liveStreamingDetails": {
      "actualStartTime": "2024-01-01T10:00:00Z"
    }
  }]
}
```

### データストレージ

#### データモデル
- 一時的なメモリ保持のみ（永続化なし）
- VideoSyncInfo: 計算結果の一時保存
- APIレスポンス: キャッシュなしで毎回取得

#### ストレージ選択理由
- データ永続化は不要（同期機能の基盤として一時的な計算のみ）

## 設計アプローチ比較

> 検討された他のアプローチとその選択結果

### 検討した他のアプローチ

#### サーバー経由でのAPI取得
**メリット**:
- APIキーの安全な管理
- レート制限の一元管理
- セキュリティ向上

**デメリット**:
- サーバーサイドの実装が必要
- レスポンス時間の増加
- 追加のインフラ管理が必要

**選択しなかった理由**: 初期実装としては過度に複雑で、シンプルな要件に対して過剰設計

#### 動画情報の事前取得・キャッシュ
**メリット**:
- レスポンス時間の向上
- オフライン対応

**デメリット**:
- データ管理の複雑化
- 同期の課題
- ストレージ容量の使用

**選択しなかった理由**: データ永続化が不要という要件に合致しない

### サードパーティ/オープンソース検討
- YouTube Data API v3: 採用 - YouTube公式APIで信頼性が高い
- Retrofit/Ktor Client: 検討中 - 既存のHTTPクライアント実装に合わせる

## 実装計画

> Serena分析に基づく具体的な実装ロードマップ

### 実装優先度
**High Priority** (必須機能):
- [ ] Interface定義とデータモデル実装 - 期限: Week 1
- [ ] VideoSyncUseCase実装 - 期限: Week 2
- [ ] PlaybackPositionProvider実装 - 期限: Week 2

**Medium Priority** (重要機能):
- [ ] VideoSyncRepository実装 - 期限: Week 2
- [ ] 同期ボタンUI実装 - 期限: Week 3
- [ ] エラーハンドリング実装 - 期限: Week 3

**Low Priority** (拡張機能):
- [ ] ログ・アナリティクス実装 - 期限: Week 4

### 開発マイルストーン

#### Milestone 1: 基盤実装 (Week 1-2)
- [ ] Interface定義とデータモデル
- [ ] YouTube Data API v3設定
- [ ] APIキー管理システム

#### Milestone 2: コア機能実装 (Week 2-3)
- [ ] VideoSyncUseCase実装
- [ ] PlaybackPositionProvider実装
- [ ] VideoSyncRepository実装

#### Milestone 3: UI統合・テスト (Week 3-4)
- [ ] 同期ボタンUI実装
- [ ] エラーハンドリング
- [ ] 単体テスト・統合テスト

#### Milestone 4: 最終調整・リリース (Week 4)
- [ ] バグ修正
- [ ] パフォーマンステスト
- [ ] ドキュメント整備

## リスク管理

### 技術的リスク
**High Risk**:
- **APIキー管理**: クライアントサイドでの露出リスク - 対応策: 設定ファイルでの管理、将来的なサーバー経由への移行準備
- **YouTube API制限**: レート制限やクォータ制限 - 対応策: エラーハンドリングと適切な利用率監視

**Medium Risk**:
- **プラットフォーム差異**: AndroidとiOSでの実装差異 - 対応策: 十分なテストと統一的なInterface設計

### プロジェクトリスク
**スケジュールリスク**:
- YouTube Player APIの学習コスト → 段階的な実装とプロトタイプ作成

**リソースリスク**:
- プラットフォーム固有の知識不足 → 既存実装の活用と段階的な学習

### 品質リスク
**パフォーマンス**:
- API呼び出しの遅延 → タイムアウト設定とエラーハンドリング

**互換性**:
- YouTube Player APIの変更 → 公式ドキュメントの定期確認

## ログ・GA設計

### ログ設計
- 同期ボタンクリックイベント
- API取得成功/失敗
- 計算処理時間

### 分析設計
- 同期機能の利用率トラッキング
- エラー発生率の監視
- レスポンス時間の分析

## セキュリティの考慮

### 認証・認可
- YouTube Data API v3のAPIキー認証

### データ保護
- 一時的なメモリ保持のみで永続化なし
- APIキーの適切な管理

### 脆弱性対策
- 入力値検証（動画ID形式チェック）
- HTTPSによる通信暗号化

## 可観測性

### モニタリング
- API呼び出し成功率
- レスポンス時間
- エラー発生率

### ログ収集
- アプリケーションログでのイベント記録
- APIレスポンスログ

## 成功指標と検証

### 技術的成功指標
- **パフォーマンス**: API取得から結果表示まで3秒以内
- **品質**: 時刻計算精度1秒以内、API成功率95%以上
- **保守性**: Interface設計による疎結合実現

### ビジネス成功指標
- **ユーザー満足度**: 同期機能の基盤として活用される
- **利用率**: 同期ボタンの使用頻度
- **機能効果**: 将来の同期機能への円滑な拡張

### 検証計画
**Pre-Release**:
- [ ] 単体テスト: 各Interfaceの実装
- [ ] 統合テスト: API連携とUI動作
- [ ] パフォーマンステスト: レスポンス時間測定

**Post-Release**:
- [ ] 利用率監視: 同期ボタンの使用状況
- [ ] エラー率監視: API失敗率とユーザーエラー

## 参考資料

### Serena分析レポート
- VideoPlayerView既存実装の分析
- プラットフォーム固有パターンの確認

### 関連ドキュメント
- YouTube Data API v3 Documentation
- YouTube Player API Documentation
- Kotlin Multiplatform Guide

### コードベース参照
- VideoPlayerView実装: `composeApp/src/androidMain/kotlin/org/example/project/video/VideoPlayerView.android.kt:23`
- iOS WebView実装: `composeApp/src/iosMain/kotlin/org/example/project/video/VideoPlayerView.ios.kt:14`

---

**設計アプローチ**: クライアント直接API呼び出し | **Interface設計**: 完了 | **作成者**: Claude Code
**作成日**: 2025-01-14 | **最終更新**: 2025-01-14 | **レビュー担当**: TBD