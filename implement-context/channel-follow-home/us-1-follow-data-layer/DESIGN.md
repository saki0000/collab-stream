# 設計メモ: チャンネルフォロー データ層

> **US**: US-1（チャンネルフォロー データ層）
> **SPECIFICATION**: `feature/channel_follow/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Data | `data/local/entity/FollowedChannelEntity.kt` | Room Entity（followed_channel テーブル） |
| Shared Data | `data/local/FollowedChannelDao.kt` | Room DAO（CRUD + Flow監視） |
| Shared Data | `data/local/FollowedChannelMapper.kt` | Entity ↔ Domain 変換 |
| Shared Data | `data/local/AppDatabase.kt` | DAO追加 + version 2 + マイグレーション |
| Shared Data | `data/repository/ChannelFollowRepositoryImpl.kt` | Repository実装 |
| Shared DI | `di/DatabaseModule.kt` | DAO + Repository登録 |
| Shared Test | `data/repository/ChannelFollowRepositoryImplTest.kt` | Repository単体テスト |

### 既存コードとの関連

- 参考実装: `SyncHistoryDao` + `SyncHistoryRepositoryImpl`（同じRoom KMPパターン）
- 既存: `FollowedChannel`（Domain Model）, `ChannelFollowRepository`（Interface）は定義済み
- DB基盤: `AppDatabase`, `DatabaseBuilder`, `Converters` は既存
- 準拠ADR: ADR-001（Android Architecture）

---

## 技術的な注意点

- **DBマイグレーション**: version 1→2。`followed_channel`テーブル追加のAUTO_MIGRATION
- **複合キー**: `channelId` + `serviceType` で一意性を保証（同じチャンネルIDでもYouTube/Twitchで別扱い）
- **TypeConverter**: `VideoServiceType` は既存の `Converters` を利用（String変換済み）
- **Instant保存**: `followedAt` は `Long`（epochMillis）で保存、Mapperで変換
- **Clock**: `kotlin.time.Clock.System.now()` を使用（`kotlinx.datetime.Clock` は非推奨）
