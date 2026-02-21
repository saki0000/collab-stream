# 設計メモ: 匿名認証（デバイスID）

> **US**: US-2（匿名認証）
> **SPECIFICATION**: `feature/subscription/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Data | `data/local/entity/UserDeviceEntity.kt` | Room Entity（user_device テーブル、単一行） |
| Shared Data | `data/local/UserDeviceDao.kt` | Room DAO（getDeviceId / insert） |
| Shared Data | `data/local/AppDatabase.kt` | UserDeviceEntity追加 + DAO getter + version 3 + AutoMigration |
| Shared Data | `data/repository/UserRepositoryImpl.kt` | UserRepository実装（ID生成 + 永続化） |
| Shared DI | `di/DatabaseModule.kt` | UserDeviceDao + UserRepository登録 |
| Shared Test | `data/repository/UserRepositoryImplTest.kt` | Repository単体テスト |

### 既存コードとの関連

- 参考実装: `ChannelFollowRepositoryImpl`（同じRoom KMP + DAOパターン）
- 既存: `UserRepository`（Interface）は US-1 で定義済み
- DB基盤: `AppDatabase` version 2 → 3 への AutoMigration
- 準拠ADR: ADR-001（Android Architecture）

---

## 技術的な注意点

- **単一行テーブル**: `user_device` テーブルは常に1行のみ（`id = 1` 固定PK）
- **UUID v4生成**: `kotlin.uuid.Uuid.random().toString()` を使用
- **DBマイグレーション**: version 2→3。`user_device` テーブル追加の AutoMigration
- **スレッドセーフ**: `getDeviceId()` は初回呼び出し時にUUID生成 + INSERT。Room の suspend 関数により直列化される
- **ComposeApp / Server の変更なし**: データ層のみの実装
