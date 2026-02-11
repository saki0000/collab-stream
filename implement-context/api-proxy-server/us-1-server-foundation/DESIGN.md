# 設計メモ: 共通基盤 & サーバー骨格構築

> **US**: `implement-context/api-proxy-server/us-1-server-foundation/US.md`
> **SPECIFICATION**: `server/src/main/kotlin/org/example/project/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Domain | `shared/.../domain/model/ApiResponse.kt` | 共通APIレスポンスDTO（sealed class） |
| Server | `server/.../Application.kt` | プラグインインストール呼び出し追加 |
| Server | `server/.../plugins/Serialization.kt` | ContentNegotiation設定 |
| Server | `server/.../plugins/StatusPages.kt` | エラーハンドリング設定 |
| Server | `server/.../plugins/Cors.kt` | CORS設定 |
| Server | `server/.../routes/HealthRoutes.kt` | ヘルスチェックエンドポイント |
| Server | `server/.../config/ApiKeyConfig.kt` | 環境変数からのAPIキー読み込み |
| Server | `server/build.gradle.kts` | Ktor server依存追加（content-negotiation, status-pages, cors） |
| Server Test | `server/src/test/.../HealthRoutesTest.kt` | ヘルスチェックテスト |
| Server Test | `server/src/test/.../StatusPagesTest.kt` | エラーハンドリングテスト |

### 既存コードとの関連

- 参考実装: Ktor client の ContentNegotiation 設定（`VideoSyncRepositoryImpl.createHttpClient()`）
- 準拠ADR: ADR-001（Android Architecture）, ADR-005（段階的APIセキュリティ Phase 2）
- Ktor規約: `.claude/rules/server/ktor-rules.md`

---

## 技術的な注意点

- `ApiResponse<T>` はsharedモジュールに配置し、server/client双方から利用可能にする
- `@Serializable` が必要なため、shared の build.gradle.kts で serialization プラグインが有効であることを確認
- Ktor server の ContentNegotiation / StatusPages / CORS は別途依存追加が必要（現在のbuild.gradle.ktsには未設定）
- APIキーは環境変数から読み込み、未設定時はログ警告のみ（起動は許可）
- ヘルスチェックは認証不要のパブリックエンドポイント
