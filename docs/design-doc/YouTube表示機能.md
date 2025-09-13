# Design Document: YouTube表示機能

## 機能概要

> YouTubeの埋め込み表示機能を実装し、動画の再生を可能にします。この機能は将来的な動画同期機能の基盤となり、ユーザーが実際の動画コンテンツを視聴できる環境を提供します。

Android/iOS プラットフォームにおいて、それぞれのネイティブ実装を活用したYouTube動画プレーヤーをCompose Multiplatformアプリに統合します。Android では YouTube Android Player API を、iOS では WKWebView によるiframe埋め込みを使用し、プラットフォーム間の差異をexpect/actualパターンで吸収します。

この実装により、ユーザーは将来実装予定の動画同期機能において、実際の動画を見ながら同期操作を行うことができるようになります。

## 実装背景

### 課題・問題点
- 動画同期機能の実装に向けて、実際の動画再生環境が必要
- プラットフォーム固有のYouTube埋め込み実装が必要
- 統一されたインターフェースでのマルチプラットフォーム対応

### 戦略との関連
- **技術的戦略**: Kotlin Multiplatformによるコード共有とプラットフォーム固有実装の適切な分離
- **製品戦略**: 動画同期機能の基盤となるプレーヤー機能の確立
- **四半期目標**: 動画関連機能の開発基盤構築

## 影響・対象範囲

### 対象システム・コンポーネント
- 新規作成（既存システムへの影響なし）

### 対象ユーザー
- エンドユーザー

### 対象プラットフォーム
- Android
- iOS

## ゴール

### ユーザーへの影響
- 実際のYouTube動画を視聴できる環境の提供
- 将来的な動画同期機能の利用準備
- プラットフォーム間で一貫した動画視聴体験

### 成功指標
- **動画再生成功率**: 95%以上
- **読み込み時間**: 3秒以内
- **エラー発生率**: 5%以下

### トラッキング
- 動画読み込み状況のログ監視
- エラー発生状況の分析

## 対象外項目

- 動画同期機能の実装
- 複数動画の同時再生
- 動画の再生位置制御（シーク機能）
- Web (WASM) プラットフォーム対応

## 解決策 / 技術アーキテクチャ

### 全体概要

expect/actualパターンを使用してプラットフォーム固有のYouTube動画プレーヤーを実装します。共通インターフェースとしてComposable関数を定義し、Android・iOS それぞれで最適な実装を提供します。

### Interface設計

> **必須セクション**: Presentation層のみの実装におけるInterface定義と責務明確化

#### Core Interfaces

**Presentation Layer (composeApp)**
```kotlin
// 共通データモデル
data class VideoUiState(
    val videoId: String = "",
    val serviceType: VideoServiceType = VideoServiceType.YOUTUBE,
    val syncDateTime: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class VideoServiceType {
    YOUTUBE
}

// expect/actual Pattern
expect fun VideoPlayerView(
    videoId: String,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
): @Composable Unit
```

**Android実装 (androidMain)**
```kotlin
actual fun VideoPlayerView(
    videoId: String,
    modifier: Modifier,
    onError: (String) -> Unit
): @Composable Unit {
    // YouTube Android Player API + AndroidView 実装
}
```

**iOS実装 (iosMain)**
```kotlin
actual fun VideoPlayerView(
    videoId: String,
    modifier: Modifier,
    onError: (String) -> Unit
): @Composable Unit {
    // WKWebView + UIKitView + iframe 実装
}
```

#### Interface責務マトリックス

| Layer | Interface | 主な責務 |
|-------|-----------|----------|
| Presentation | VideoUiState | 動画表示状態の管理 |
| Presentation | VideoPlayerView (expect) | プラットフォーム非依存インターフェース |
| Presentation | VideoPlayerView (actual Android) | Android固有プレーヤー実装 |
| Presentation | VideoPlayerView (actual iOS) | iOS固有プレーヤー実装 |

#### 実装指針

**Interface設計原則:**
- 単一責任: 各コンポーネントは動画表示のみに責任を持つ
- プラットフォーム分離: expect/actualによる適切な実装分離
- エラーハンドリング: 統一されたエラー処理インターフェース

### 詳細設計

#### ComposeApp Module (UI)
- `VideoPlayerView`: expect/actual による統一インターフェース
- `VideoUiState`: 動画表示状態管理
- エラー処理: Snackbar による表示

#### プラットフォーム固有実装

**Android**
- YouTube Android Player API の利用
- AndroidView による Compose 統合
- ネイティブプレーヤーコントロール

**iOS**
- WKWebView によるiframe埋め込み
- UIKitView による Compose 統合
- Webベースプレーヤーコントロール

### API

今回の実装ではAPI設計は不要です（埋め込み表示のみ）。

### データストレージ

今回の実装ではデータ永続化は不要です。UIState での状態管理のみ。

## 代替案

### 案1: WebView統一実装
**メリット**:
- プラットフォーム間で同一のコード実装
- メンテナンスコストの削減

**デメリット**:
- Android でのパフォーマンス劣化
- ネイティブ機能へのアクセス制限

### サードパーティ/オープンソース検討
- YouTube Data API: 動画情報取得のみで埋め込み再生には不適
- ExoPlayer: YouTube 動画の直接再生は規約上制限

## マイルストーン

### フェーズ1: 設計・要件整理
- [x] 要件定義とInterface設計
- [x] プラットフォーム別実装方針決定

### フェーズ2: 実装
- [ ] expect interface 定義
- [ ] Android actual 実装（YouTube Android Player API）
- [ ] iOS actual 実装（WKWebView + iframe）
- [ ] エラーハンドリング実装

### フェーズ3: 内部レビュー・テスト
- [ ] プラットフォーム別動作確認
- [ ] エラーケースのテスト

### フェーズ4: UAT
- [ ] エンドユーザーでの動作確認

### フェーズ5: リリース
- [ ] プロダクションデプロイ

## 実装の懸念点

### 技術的リスク
- プラットフォーム間でのAPIの差異による実装複雑化
- YouTube Player API の制限や変更
- ネットワーク状況による動画読み込み失敗

### 緩和策
- 十分なエラーハンドリングとフォールバック実装
- プラットフォーム別テストの充実
- ログによる問題箇所の特定体制

## ログ・GA設計

### ログ設計
- 動画読み込み開始/完了イベント
- エラー発生時の詳細情報
- プラットフォーム別利用状況

### 分析設計
- 動画視聴開始率
- エラー発生率の監視
- プラットフォーム別パフォーマンス比較

## セキュリティの考慮

### 認証・認可
- YouTube埋め込みに関する特別な認証は不要

### データ保護
- 動画ID以外の個人情報は扱わない
- 通信はHTTPS必須

### 脆弱性対策
- iframe埋め込み時のCSP設定
- 不正な動画IDに対する入力値検証

## 可観測性

### モニタリング
- 動画読み込み成功/失敗率
- 読み込み時間の監視
- メモリ使用量の監視

### ログ収集
- プラットフォーム別エラーログ
- パフォーマンスメトリクス

### トレーシング
- 動画読み込みフローの追跡

## 参考資料

- [YouTube Android Player API](https://developers.google.com/youtube/android/player)
- [WKWebView Documentation](https://developer.apple.com/documentation/webkit/wkwebview)
- [Compose Multiplatform Platform-specific APIs](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-platform-specific-apis.html)

---

**作成者**: Claude Code  
**作成日**: 2024-09-13  
**最終更新**: 2024-09-13  
**レビュー担当**: 未定