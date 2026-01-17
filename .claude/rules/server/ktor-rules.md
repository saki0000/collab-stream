---
paths: server/**/*.kt
---

# Ktor サーバー規約

## 構造

```
server/src/main/kotlin/org/example/project/
├── Application.kt    # エントリーポイント
├── plugins/          # Ktor プラグイン設定
├── routes/           # ルーティング
└── models/           # リクエスト/レスポンスモデル
```

## エンドポイント設計

### REST 規約

| メソッド | パス | 用途 |
|---------|------|------|
| GET | `/api/videos` | 一覧取得 |
| GET | `/api/videos/{id}` | 単体取得 |
| POST | `/api/videos` | 新規作成 |
| PUT | `/api/videos/{id}` | 更新 |
| DELETE | `/api/videos/{id}` | 削除 |

### ルーティング例

```kotlin
fun Route.videoRoutes() {
    route("/api/videos") {
        get {
            val videos = videoService.getAll()
            call.respond(videos)
        }

        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val video = videoService.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(video)
        }
    }
}
```

## エラーハンドリング

```kotlin
install(StatusPages) {
    exception<NotFoundException> { call, cause ->
        call.respond(HttpStatusCode.NotFound, ErrorResponse(cause.message))
    }
    exception<IllegalArgumentException> { call, cause ->
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message))
    }
}
```

## シリアライゼーション

- `kotlinx.serialization` を使用
- `ContentNegotiation` プラグインで設定

```kotlin
install(ContentNegotiation) {
    json(Json {
        prettyPrint = true
        isLenient = true
    })
}
```

## ポート

- デフォルト: **8080**
- 設定: `shared/src/commonMain/kotlin/org/example/project/Constants.kt`

## テスト

```kotlin
@Test
fun `GET videos returns list`() = testApplication {
    application {
        configureRouting()
    }
    client.get("/api/videos").apply {
        assertEquals(HttpStatusCode.OK, status)
    }
}
```
