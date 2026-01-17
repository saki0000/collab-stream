# 技術仕様: ローカル永続化基盤（Room KMP）

> **配置場所**: `shared/src/commonMain/kotlin/org/example/project/data/local/REQUIREMENTS.md`
> **目的**: AI実装のためのSSoT（Single Source of Truth）
> **Story**: US-1 of EPIC-003 (同期チャンネル履歴保存)

---

## 1. 技術ストーリー

開発者として基盤を利用する視点で記述します。

- 開発者として、Room KMPを使用してローカルデータベースにエンティティを永続化できる
- 開発者として、Android/iOSプラットフォームで同じDatabase/Daoコードを共有できる
- 開発者として、KoinでDatabaseインスタンスとDAOを注入できる
- 開発者として、DAOを通じてCRUD操作（作成、読取、更新、削除）を実行できる
- 開発者として、Flowを使用してデータ変更をリアクティブに監視できる
- 開発者として、BundledSQLiteDriverにより全プラットフォームで一貫した動作を期待できる

---

## 2. 技術仕様

### 2.1 依存関係（gradle/libs.versions.toml）

> **Note**: バージョンはPhase 2実装時に最新の安定版/互換版を確認してください。
> 以下は参考バージョンです。

```toml
[versions]
# Room KMP: https://developer.android.com/jetpack/androidx/releases/room
# Phase 2で最新KMP対応バージョンを確認（2.7.0-alpha系 or 安定版）
room = "2.7.0-alpha03"
# SQLite Bundled: https://developer.android.com/jetpack/androidx/releases/sqlite
sqlite = "2.4.0"
# KSP: Kotlinバージョンに対応（例: Kotlin 2.0.21 → ksp 2.0.21-1.0.27）
# プロジェクトのKotlinバージョンに合わせて調整
ksp = "2.0.21-1.0.27"

[libraries]
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

### 2.2 KSP設定（shared/build.gradle.kts）

```kotlin
plugins {
    id("com.google.devtools.ksp")
    id("androidx.room")
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Android
    add("kspAndroid", libs.androidx.room.compiler)
    // iOS
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}
```

### 2.3 Database基盤クラス

**AppDatabase.kt** (commonMain):
```kotlin
@Database(
    entities = [SyncHistoryEntity::class, SavedChannelEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncHistoryDao(): SyncHistoryDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase>
```

**DatabaseBuilder.kt** (expect/actual):
```kotlin
// commonMain
expect class DatabaseBuilder {
    fun build(): AppDatabase
}

// androidMain
actual class DatabaseBuilder(private val context: Context) {
    actual fun build(): AppDatabase {
        val dbFile = context.getDatabasePath("collabstream.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

// iosMain
actual class DatabaseBuilder {
    actual fun build(): AppDatabase {
        val dbFilePath = NSHomeDirectory() + "/Documents/collabstream.db"
        return Room.databaseBuilder<AppDatabase>(name = dbFilePath)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}
```

### 2.4 Entity定義

**SyncHistoryEntity.kt**:
```kotlin
@Entity(tableName = "sync_history")
data class SyncHistoryEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val createdAt: Long,      // epochMillis
    val lastUsedAt: Long,     // epochMillis
    val usageCount: Int
)
```

**SavedChannelEntity.kt**:
```kotlin
@Entity(
    tableName = "saved_channel",
    foreignKeys = [ForeignKey(
        entity = SyncHistoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["historyId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("historyId")]
)
data class SavedChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val historyId: String,
    val channelId: String,
    val channelName: String,
    val channelIconUrl: String,
    val serviceType: VideoServiceType  // TypeConverterで変換
)
```

**SyncHistoryWithChannels.kt** (Relation):
```kotlin
data class SyncHistoryWithChannels(
    @Embedded val history: SyncHistoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "historyId"
    )
    val channels: List<SavedChannelEntity>
)
```

### 2.5 DAO定義

**SyncHistoryDao.kt**:
```kotlin
@Dao
interface SyncHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: SyncHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<SavedChannelEntity>)

    /**
     * 履歴とチャンネルをアトミックに保存する。
     *
     * insert()とinsertChannels()を別々に呼ぶと不整合リスクがあるため、
     * このトランザクションメソッドを使用することを推奨。
     */
    @Transaction
    suspend fun insertHistoryWithChannels(
        history: SyncHistoryEntity,
        channels: List<SavedChannelEntity>
    ) {
        insert(history)
        insertChannels(channels)
    }

    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY lastUsedAt DESC")
    fun observeAllByLastUsed(): Flow<List<SyncHistoryWithChannels>>

    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY createdAt DESC")
    fun observeAllByCreated(): Flow<List<SyncHistoryWithChannels>>

    @Transaction
    @Query("SELECT * FROM sync_history ORDER BY usageCount DESC")
    fun observeAllByMostUsed(): Flow<List<SyncHistoryWithChannels>>

    @Transaction
    @Query("SELECT * FROM sync_history WHERE id = :id")
    suspend fun getById(id: String): SyncHistoryWithChannels?

    @Query("DELETE FROM sync_history WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE sync_history SET usageCount = usageCount + 1, lastUsedAt = :timestamp WHERE id = :id")
    suspend fun recordUsage(id: String, timestamp: Long)

    @Query("UPDATE sync_history SET name = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String?)
}
```

### 2.6 TypeConverter

**Converters.kt**:
```kotlin
import org.example.project.domain.model.VideoServiceType

class Converters {
    @TypeConverter
    fun fromServiceType(value: VideoServiceType): String = value.name

    @TypeConverter
    fun toServiceType(value: String): VideoServiceType = VideoServiceType.valueOf(value)
}
```

> **Note**: ドメイン層のVideoServiceType Enumを直接使用することで型安全性を確保。
> 不正な文字列がデータベースに保存されるのを防ぎます。

### 2.7 Koin DI設定

**DatabaseModule.kt**:
```kotlin
val databaseModule = module {
    single<AppDatabase> { get<DatabaseBuilder>().build() }
    single { get<AppDatabase>().syncHistoryDao() }
}
```

**プラットフォーム固有モジュール**:
```kotlin
// androidMain
val androidDatabaseModule = module {
    single { DatabaseBuilder(androidContext()) }
}

// iosMain
val iosDatabaseModule = module {
    single { DatabaseBuilder() }
}
```

### 2.8 プラットフォームサポート

| プラットフォーム | サポート状況 | 備考 |
|----------------|-------------|------|
| Android | ✅ | Context経由でDB path取得 |
| iOS | ✅ | NSHomeDirectory使用 |
| WASM | ❌ | Room KMP未対応 |
| JVM (Server) | ❌ | サーバーでの永続化は別途設計 |

---

## 3. 検証方法

### 3.1 ビルド検証
- [ ] `./gradlew :shared:build` が成功すること
- [ ] KSP生成コードがエラーなく生成されること
- [ ] schemas/ディレクトリにスキーマJSONが出力されること

### 3.2 テスト検証
- [ ] `./gradlew :shared:test` が成功すること
- [ ] SyncHistoryDaoTest がすべてパスすること
- [ ] インメモリデータベースでのCRUD操作が正常動作すること

### 3.3 プラットフォーム検証
- [ ] Android実機/エミュレータでデータベースファイルが作成されること
- [ ] iOSシミュレータでデータベースファイルが作成されること
- [ ] アプリ再起動後もデータが永続化されていること

---

## 4. Phase 2実装進捗

**Phase 1完了時に作成し、Phase 2実装中に随時更新します。**

**最終更新**: 2026-01-17

### Gradle設定
- [x] libs.versions.toml にRoom依存関係追加
- [x] shared/build.gradle.kts にKSP設定追加
- [x] Room Gradleプラグイン設定

### Database基盤
- [x] AppDatabase実装
- [x] AppDatabaseConstructor (expect/actual)
- [x] DatabaseBuilder (expect/actual)
- [x] Converters（TypeConverter）

### Entity/DAO
- [x] SyncHistoryEntity実装
- [x] SavedChannelEntity実装
- [x] SyncHistoryWithChannels（Relation）実装
- [x] SyncHistoryDao実装

### Mapper
- [x] SyncHistoryMapper（Entity ↔ Domain変換）

### Repository実装
- [x] SyncHistoryRepositoryImpl（SyncHistoryRepositoryインターフェース実装）

### DI設定
- [x] DatabaseModule実装
- [x] プラットフォーム固有モジュール（Android/iOS）
- [x] SharedModuleへの統合

### テスト
- [ ] SyncHistoryDaoTest実装（テスト骨格のみ、完全実装は別途）
- [x] `./gradlew :composeApp:assembleDebug` 成功

---

## 補足

### 使用するドメインモデル（Phase 0で定義済み）

- `SyncHistory` - 同期セッション履歴（domain/model/SyncHistory.kt）
- `SavedChannelInfo` - 保存チャンネル情報（domain/model/SavedChannelInfo.kt）
- `SyncHistoryRepository` - リポジトリインターフェース（domain/repository/SyncHistoryRepository.kt）
- `HistorySortOrder` - ソート順列挙型（LAST_USED, CREATED, MOST_USED）

### 参照

- **Room KMP公式ドキュメント**: https://developer.android.com/kotlin/multiplatform/room
- **Epic定義**: EPIC-003（同期チャンネル履歴保存）Issue #35
- **類似パターン**: `data/repository/`（既存Repository実装の参考）
- **参照ADR**: ADR-002（MVIパターン）, ADR-003（4層コンポーネント構造）

### 注意事項

- **BundledSQLiteDriver**: バイナリサイズ増加（Android/iOS両方で約1-2MB）
- **TypeConverter**: Instant ↔ Long変換はEntity設計で直接Long使用により回避
- **Foreign Key**: CASCADE削除でチャンネルも自動削除
- **Index**: historyIdカラムにインデックス追加でクエリ高速化

---

**作成者**: Claude Code
**作成日**: 2026-01-14
**関連Issue**: #36
**Epic**: 同期チャンネル履歴保存 (EPIC-003)
