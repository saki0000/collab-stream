# 設計メモ: 外部アプリ連携（DeepLink）

> **US**: Timeline Sync US-4
> **SPECIFICATION**: `feature/timeline_sync/SPECIFICATION.md`

---

## 実装方針

### 変更対象

| レイヤー | ファイル | 変更内容 |
|---------|---------|---------|
| Shared Domain | `shared/.../domain/model/DeepLinkInfo.kt` | DeepLink URL生成ロジック（新規） |
| Shared Test | `shared/src/commonTest/.../DeepLinkInfoTest.kt` | URL生成のユニットテスト（新規） |
| ComposeApp | `feature/timeline_sync/TimelineSyncIntent.kt` | OpenExternalApp Intent追加 + SideEffect更新 |
| ComposeApp | `feature/timeline_sync/TimelineSyncViewModel.kt` | Intent処理 + OPENED状態更新 |
| ComposeApp | `feature/timeline_sync/ui/TimelineSyncContainer.kt` | SideEffect処理（LocalUriHandler） |
| ComposeApp | `feature/timeline_sync/ui/components/TimelineCard.kt` | Open/Waitボタン有効化 + OPENED表示 |
| ComposeApp | `feature/timeline_sync/ui/components/TimelineCardsWithSyncLine.kt` | onOpenClickコールバック接続 |
| ComposeApp | `feature/timeline_sync/ui/TimelineSyncContent.kt` | onOpenClickコールバック伝播 |
| ComposeApp Test | `commonTest/.../TimelineSyncViewModelTest.kt` | ViewModel Intent処理テスト |

### 既存コードとの関連

- **プレースホルダー箇所**: TimelineCard.kt の `{ /* Story 4 */ }` を実装に置換
- **SideEffect**: `NavigateToExternalApp` が既に定義済み（URLフィールド追加が必要）
- **SyncStatus.OPENED**: ドメインモデルに定義済み（使用開始）
- **targetSeekPosition**: US-3で計算済み（そのまま利用）
- 準拠ADR: ADR-001, ADR-002, ADR-004

---

## 技術的な設計

### DeepLink URL仕様

| Platform | DeepLink | フォールバック |
|----------|----------|---------------|
| YouTube | `youtube://watch?v={VIDEO_ID}&t={SECONDS}` | `https://www.youtube.com/watch?v={VIDEO_ID}&t={SECONDS}s` |
| Twitch | `twitch://video/{VIDEO_ID}?t={SECONDS}s` | `https://www.twitch.tv/videos/{VIDEO_ID}?t={SECONDS}s` |

- `SECONDS` = `targetSeekPosition.toInt().coerceAtLeast(0)`
- YouTubeのDeepLinkは `t=` に `s` サフィックスなし、WebURLは `s` あり

### 外部アプリ起動フロー

```
OpenExternalApp Intent
  → ViewModel: チャンネル取得 → DeepLinkInfo生成 → SideEffect emit → OPENED更新
    → Container: LocalUriHandler で DeepLink URI を試行
      → 成功: 何もしない（SideEffectで既にOPENED更新済み）
      → 失敗: フォールバックURL を試行
        → 失敗: ShowError SideEffect
```

### Open/Waitボタンの状態遷移

| SyncStatus | ボタン表示 | 有効/無効 | アイコン |
|-----------|-----------|----------|---------|
| NOT_SYNCED | `--` | 無効 | なし |
| WAITING | `Wait` | 無効 | Lock |
| READY | `Open` | **有効** | OpenInNew |
| OPENED | `Open ✓` | **有効**（再タップ可） | Check |

### Compose UriHandler活用

`LocalUriHandler` はCompose Multiplatformで Android/iOS 両対応。
Container（Stateful層）でのみ使用し、Screen以下には渡さない。

---

## 技術的な注意点

- `LocalUriHandler.openUri()` は失敗時に例外をスロー → try-catch で処理
- SyncStatus変更はsyncTime変更時に再計算されるため、OPENED→ドラッグ→READYの再計算で上書きされる可能性あり → `calculateChannelSyncInfo` でOPENED維持ロジック追加
- DeepLink URLの生成は純粋関数としてShared層に配置し、テスタビリティを確保
