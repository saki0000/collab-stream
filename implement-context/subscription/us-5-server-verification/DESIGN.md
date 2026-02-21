# 設計メモ: サーバーサイドサブスクリプション検証API

> **US**: implement-context/subscription/us-5-server-verification/US.md
> **SPECIFICATION**: `feature/subscription/server/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Server | `server/.../config/ApiKeyConfig.kt` | `revenueCatApiKey` 追加 |
| Server | `server/.../service/SubscriptionService.kt` | インターフェース新規作成 |
| Server | `server/.../service/SubscriptionServiceImpl.kt` | RevenueCat REST API v1連携実装 |
| Server | `server/.../routes/SubscriptionRoutes.kt` | `/api/subscription/status` エンドポイント |
| Server | `server/.../Application.kt` | SubscriptionService初期化 + ルーティング登録 |
| Server Test | `server/.../routes/SubscriptionRoutesTest.kt` | ルーティングテスト |
| Server Test | `server/.../service/SubscriptionServiceImplTest.kt` | サービス層テスト |

### ComposeApp / Shared は変更なし

- `SubscriptionStatus` / `SubscriptionTier` は既にsharedモジュールに定義済み
- サーバーは `projects.shared` を依存しているため、ドメインモデルを直接使用可能

### 既存コードとの関連

- 参考実装: `server/.../routes/CommentRoutes.kt` + `service/CommentServiceImpl.kt`（最もシンプルなRoute + Service パターン）
- エラーハンドリング: 既存の `StatusPages` を活用（ExternalApiException, ServiceUnavailableException）
- APIキー管理: `ApiKeyConfig` に `revenueCatApiKey` を追加する既存パターン踏襲
- レスポンス形式: `ApiResponse.Success<SubscriptionStatus>` で統一

---

## 技術的な注意点

- RevenueCat REST API v1 は `GET /v1/subscribers/{appUserId}` で、Bearer token認証が必要（Secret API Key）
- 環境変数 `REVENUECAT_API_KEY` でSecret API Keyを管理
- RevenueCatで未登録のユーザーの場合、空のsubscriber objectが返る可能性がある → FREEプランとして扱う
- entitlement名は `"pro"` を使用（RevenueCatダッシュボードでの設定に依存）
- `SubscriptionStatus` は `@Serializable` 付きのため、Ktorのレスポンスシリアライゼーションでそのまま使用可能
