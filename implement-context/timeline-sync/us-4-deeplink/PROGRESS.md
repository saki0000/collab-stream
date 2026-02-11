# 進捗管理: 外部アプリ連携（DeepLink）

> **US**: Timeline Sync US-4
> **SPECIFICATION**: `feature/timeline_sync/SPECIFICATION.md`
> **ブランチ**: `feature/timeline-sync-us4-deeplink`

---

## Shared Layer

### Domain Model
- [x] `DeepLinkInfo.kt` - DeepLink URL / フォールバックURL のデータクラス + SyncChannelからの生成ロジック

### Shared テスト
- [x] `DeepLinkInfoTest.kt` - YouTube/Twitch URL生成テスト
- [x] `./gradlew :shared:build` 成功

---

## ComposeApp Layer

### Intent / SideEffect
- [x] `TimelineSyncIntent.kt` - `OpenExternalApp(channelId)` Intent追加
- [x] `TimelineSyncIntent.kt` - `NavigateToExternalApp` SideEffect にURL情報追加

### ViewModel
- [x] `TimelineSyncViewModel.kt` - `openExternalApp()` メソッド追加（DeepLinkInfo生成 + SideEffect emit + OPENED更新）
- [x] `TimelineSyncViewModel.kt` - `calculateChannelSyncInfo()` でOPENED状態維持ロジック追加

### UI（Component層）
- [x] `TimelineCard.kt` - OpenWaitButton の READY/OPENED 状態で有効化 + onClick接続 + OPENED表示追加
- [x] `TimelineCardsWithSyncLine.kt` - onOpenClick コールバックの引き回し追加

### UI（Content / Screen / Container層）
- [x] `TimelineSyncContent.kt` - onOpenClick コールバック伝播
- [x] `TimelineSyncContainer.kt` - NavigateToExternalApp SideEffect処理（LocalUriHandler + フォールバック + エラー処理）

### ComposeApp テスト
- [x] `ExternalAppNavigationViewModelTest.kt` - OpenExternalApp Intent処理テスト + OPENED状態テスト
- [x] `./gradlew :composeApp:build` 成功

---

## Integration

### 最終確認
- [x] `./gradlew test` 全テスト成功
- [x] SPECIFICATION.md の Story 4 ユーザーストーリーが全て実装済み

---

## メモ

- iOSのcompileTestが既存のTestUtils.kt (expected runTest) の問題で失敗するが、今回の変更とは無関係
- OpenWaitButton: OPENED状態でCheck アイコン + "Open" テキストで再タップ可能
- OPENED状態維持: syncTime変更時にREADY範囲内ならOPENED維持、範囲外ならWAITING/NOT_SYNCEDに遷移
