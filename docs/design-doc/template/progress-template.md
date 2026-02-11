# 進捗管理: {US Name}

> **US**: {US参照}
> **SPECIFICATION**: `feature/{feature_name}/SPECIFICATION.md`
> **ブランチ**: `feature/{feature_name}`

---

## Shared Layer

### Domain Model
- [ ] `{Model}.kt` - {概要}
- [ ] `{ValueObject}.kt` - {概要}（該当する場合）

### Repository
- [ ] `{Repository}.kt` - Interface定義
- [ ] `{RepositoryImpl}.kt` - 実装

### UseCase（該当する場合）
- [ ] `{UseCase}.kt` - {概要}

### Data Source（該当する場合）
- [ ] `{DataSource}.kt` - {概要}
- [ ] `{Mapper}.kt` - Entity ↔ DTO 変換

### Shared テスト
- [ ] `{Repository}Test.kt` または `{UseCase}Test.kt`
- [ ] `./gradlew :shared:build` 成功

---

## ComposeApp Layer

### State / Intent
- [ ] `{Feature}UiState.kt` - 画面状態定義
- [ ] `{Feature}Intent.kt` - ユーザー操作定義

### ViewModel
- [ ] `{Feature}ViewModel.kt` - MVI パターン

### UI（4層構造）
- [ ] `{Feature}Route.kt` - 状態管理・Navigation・副作用
- [ ] `{Feature}Screen.kt` - 画面レイアウト
- [ ] `{Feature}Content.kt` - 機能領域UI（状態別: Loading / Content / Error）
- [ ] `{Feature}Component.kt` - 再利用可能コンポーネント（該当する場合）

### Navigation
- [ ] Navigation graph への登録
- [ ] 呼び出し元からの遷移追加

### ComposeApp テスト
- [ ] `{Feature}ViewModelTest.kt`
- [ ] `./gradlew :composeApp:build` 成功

---

## Integration

### DI（Koin）
- [ ] Shared module 登録（Repository, UseCase）
- [ ] ComposeApp module 登録（ViewModel）

### 最終確認
- [ ] `./gradlew test` 全テスト成功
- [ ] SPECIFICATION.md の全ユーザーストーリーが実装済み

---

## メモ

実装中に気づいたこと、次回への申し送り事項などをここに記録。

- {メモ}
