---
paths: shared/**/*.kt
---

# Domain レイヤー規約

## Clean Architecture

shared モジュールは Clean Architecture に従う。

### レイヤー構造

```
shared/src/commonMain/kotlin/org/example/project/
├── domain/           # Domain レイヤー（ビジネスロジック）
│   ├── model/        # ドメインモデル、エンティティ
│   ├── repository/   # Repository インターフェース
│   └── usecase/      # ユースケース
│
└── data/             # Data レイヤー（実装）
    ├── datasource/   # データソース
    ├── repository/   # Repository 実装
    ├── mapper/       # Data → Domain マッパー
    └── model/        # API レスポンスモデル
```

## 依存関係ルール

**重要**: Data は Domain に依存できるが、逆は決してない。

```
✓ data/ → domain/  （許可）
✗ domain/ → data/  （禁止）
```

### 正しい例

```kotlin
// domain/repository/VideoRepository.kt
interface VideoRepository {
    suspend fun getVideo(id: String): Video
}

// data/repository/VideoRepositoryImpl.kt
class VideoRepositoryImpl(
    private val api: VideoApi
) : VideoRepository {
    override suspend fun getVideo(id: String): Video {
        return api.getVideo(id).toDomain()
    }
}
```

## UseCase 設計

- 1 UseCase = 1 ビジネスロジック
- `invoke()` オペレーター関数を使用
- 入力と出力を明確に定義

```kotlin
class GetVideoUseCase(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(videoId: String): Result<Video> {
        return runCatching {
            repository.getVideo(videoId)
        }
    }
}
```

## Model 設計

### Domain Model

- ビジネスロジックに必要なプロパティのみ
- プラットフォーム非依存

### Data Model

- API レスポンスに対応
- `@Serializable` アノテーション

### Mapper

- Data → Domain の変換
- 拡張関数で実装

```kotlin
// data/mapper/VideoMapper.kt
fun VideoResponse.toDomain(): Video = Video(
    id = id,
    title = title,
    duration = duration
)
```
