# Timeline Sync US-3: 同期時刻計算と表示

**Story Issue**: #53
**Epic**: #32 Timeline Sync - 利用規約対応のマルチストリーム同期機能
**Phase**: 1 (仕様定義)
**作成日**: 2026-01-17

---

## 1. ユーザーストーリー

### 1.1 同期時刻の選択

**As a** CollabStreamユーザー
**I want to** タイムラインバーをスクロールして同期時刻を選択する
**So that** 複数チャンネルの特定の時点を同時に見ることができる

#### 受け入れ基準

- ユーザーがタイムラインバーを左右にスクロールすると、画面中央の同期ラインと交差する時刻が変化する
- スクロール中は、SYNC TIME表示がリアルタイムで更新される（HH:MM:SS形式）
- スクロールを停止すると、各チャンネルのWAITING/READY状態とtargetSeekPositionが計算される
- 画面を開いた時点で、syncTimeは一番上のチャンネルのアーカイブ開始時刻に初期化される

### 1.2 チャンネル状態の表示

**As a** CollabStreamユーザー
**I want to** 各チャンネルが同期時刻で再生可能かどうかを視覚的に確認する
**So that** どのチャンネルがすぐに開けるかわかる

#### 受け入れ基準

##### WAITING状態（同期時刻がストリームの再生可能範囲外）

- 「Wait」ボタン（ロックアイコン付き、非活性）を表示
- グレーアウトされたタイムラインバー
- ボタンは押下不可
- **条件**: アーカイブ開始前（`syncTime < stream.startTime`）またはアーカイブ終了後（`syncTime > stream.endTime`）

##### READY状態（同期時刻がストリームの再生可能範囲内）

- 「Open」ボタン（外部リンクアイコン付き）を表示
- 通常色のタイムラインバー
- targetSeekPositionが計算され、外部アプリで開く準備完了
- ボタン押下で外部アプリを起動（Story 4で実装）
- **条件**: アーカイブ範囲内（`stream.startTime <= syncTime <= stream.endTime`）またはライブストリーム配信中（`endTime == null && syncTime >= stream.startTime`）

##### NOT_SYNCED状態（ストリーム未選択）

- ボタンなし、空状態表示
- タイムラインバーなし

---

## 2. ビジネスルール

### 2.1 対象動画

- **対象**: アーカイブ動画とライブストリーム（配信中の動画）
- **アーカイブ**: `endTime`が設定されている動画
- **ライブストリーム**: `endTime == null`の配信中動画（現在時刻まで再生可能）

### 2.2 スクロール範囲

- **範囲**: `syncTimeRange`（全ストリームの最早開始時刻〜最遅終了時刻）
- **制約**: syncTimeRangeの外側にはスクロールできない
- **計算方法**: UiStateの計算済みプロパティで自動算出

### 2.3 初期syncTime

- **値**: syncTimeRangeの開始時刻（一番上のチャンネルのアーカイブ開始時間）
- **タイミング**: 画面初期化時に設定

### 2.4 表示範囲

- **範囲**: syncTimeの前後30分
- **定数**: `DEFAULT_VISIBLE_DURATION = 60分`（30分 × 2）
- **スクロール**: ユーザーがスクロールすることで表示範囲を移動

### 2.5 targetSeekPosition計算

```kotlin
targetSeekPosition = (syncTime - stream.startTime).inWholeSeconds.toFloat()
```

- **型**: `Float?`
- **単位**: 秒
- **精度**: 秒単位（ミリ秒は切り捨て）
- **null条件**: WAITING状態またはNOT_SYNCED状態の場合

### 2.6 SyncStatus判定

#### NOT_SYNCED

- **条件**: syncTimeがnull、またはストリームが未選択（null）
- **表示**: ボタンなし、タイムラインバーなし

#### WAITING

- **条件**: `syncTime < stream.startTime`（同期時刻がストリーム開始前）または`syncTime > stream.endTime`（アーカイブ終了後、アーカイブのみ）
- **表示**: Waitボタン（非活性）、グレーアウトされたタイムラインバー
- **targetSeekPosition**: null

#### READY

- **条件**:
  - アーカイブ: `stream.startTime <= syncTime <= stream.endTime`（同期時刻がアーカイブ範囲内）
  - ライブストリーム: `endTime == null && syncTime >= stream.startTime`（配信開始後）
- **表示**: Openボタン（活性）、通常色のタイムラインバー
- **targetSeekPosition**: 計算値（Float）

#### 補足

- **境界値**: `syncTime == stream.startTime`はREADY状態（targetSeekPosition = 0.0f）
- **ライブストリーム**: `endTime == null`の場合、配信開始後は常にREADY状態

### 2.7 状態再計算タイミング

#### スクロール中（isDragging = true）

- **更新対象**: syncTimeのみ
- **更新方式**: 連続的（リアルタイム）
- **UI反映**: SYNC TIME表示のみ更新
- **非更新**: SyncStatus、targetSeekPosition（パフォーマンス理由）

#### スクロール停止時（isDragging = false）

- **更新対象**: SyncStatus、targetSeekPosition
- **更新方式**: 一括再計算
- **処理順序**:
  1. 全チャンネルのSyncStatus判定
  2. 全チャンネルのtargetSeekPosition計算
  3. UI状態反映

### 2.8 同期ライン表示

- **色**: 青（#0288D1）
- **位置**: 画面中央固定（縦の線）
- **太さ**: 2dp
- **動作**: スクロール時も画面中央に固定（タイムラインバーがスクロール）

---

## 3. 画面フローと状態遷移

詳細は `screen-transition.md` を参照してください。

### 主要な状態

1. **初期化中**: 画面起動直後、syncTime = null
2. **同期時刻初期化**: syncTime = syncTimeRange.first
3. **待機中**: スクロール待ち状態
4. **スクロール中**: syncTimeをリアルタイム更新、SYNC TIME表示を更新
5. **同期計算中**: SyncStatus/targetSeekPosition一括再計算

### 主要な遷移

- **初期化中 → 同期時刻初期化**: チャンネルデータ読み込み完了
- **待機中 → スクロール中**: ユーザーがタイムラインバーをスクロール開始
- **スクロール中 → 同期計算中**: スクロール停止
- **同期計算中 → 待機中**: 計算完了

---

## 4. 技術仕様

### 4.1 データモデル

#### UiState（既存）

```kotlin
data class TimelineSyncUiState(
    val syncTime: Instant?,        // 同期時刻
    val isDragging: Boolean,        // ドラッグ中フラグ
    val channels: List<SyncChannel> // チャンネルリスト
) {
    val syncTimeRange: Pair<Instant, Instant>?
        get() = calculateSyncTimeRange(channels)
}
```

#### SyncChannel（既存）

```kotlin
data class SyncChannel(
    val channelId: String,
    val selectedStream: SelectedStreamInfo?,
    val syncStatus: SyncStatus,           // Story 3で更新
    val targetSeekPosition: Float?        // Story 3で計算
)
```

#### SyncStatus（既存）

```kotlin
enum class SyncStatus {
    NOT_SYNCED,  // ストリーム未選択
    WAITING,     // 同期時刻がアーカイブ開始前
    READY        // 同期時刻がアーカイブ範囲内
}
```

### 4.2 Intent（既存）

```kotlin
sealed interface TimelineSyncIntent {
    data class UpdateSyncTime(val syncTime: Instant) : TimelineSyncIntent
    data object StartDragging : TimelineSyncIntent
    data object StopDragging : TimelineSyncIntent
}
```

### 4.3 ViewModel処理フロー

#### loadScreen()拡張

```kotlin
fun loadScreen() {
    // 既存: チャンネルデータ読み込み
    loadChannels()

    // 新規: syncTime初期化
    val range = _state.value.syncTimeRange
    if (range != null) {
        updateSyncTime(range.first)
    }
}
```

#### updateSyncTime()拡張

```kotlin
fun updateSyncTime(newSyncTime: Instant) {
    _state.update { it.copy(syncTime = newSyncTime) }

    // スクロール停止時のみ状態再計算
    if (!_state.value.isDragging) {
        recalculateSyncStatus()
        calculateAllTargetSeekPositions()
    }
}
```

#### recalculateSyncStatus()（新規）

```kotlin
private fun recalculateSyncStatus() {
    val syncTime = _state.value.syncTime
    _state.update { currentState ->
        currentState.copy(
            channels = currentState.channels.map { channel ->
                channel.copy(
                    syncStatus = calculateSyncStatus(syncTime, channel.selectedStream)
                )
            }
        )
    }
}
```

#### calculateTargetSeekPosition()（新規）

```kotlin
private fun calculateAllTargetSeekPositions() {
    val syncTime = _state.value.syncTime
    _state.update { currentState ->
        currentState.copy(
            channels = currentState.channels.map { channel ->
                channel.copy(
                    targetSeekPosition = calculateTargetSeekPosition(
                        syncTime,
                        channel.selectedStream
                    )
                )
            }
        )
    }
}

private fun calculateTargetSeekPosition(
    syncTime: Instant?,
    stream: SelectedStreamInfo?
): Float? {
    if (syncTime == null || stream == null) return null
    val startTime = stream.startTime ?: return null
    if (syncTime < startTime) return null

    return (syncTime - startTime).inWholeSeconds.toFloat()
}
```

#### calculateSyncStatus()（新規）

```kotlin
private fun calculateSyncStatus(
    syncTime: Instant?,
    stream: SelectedStreamInfo?
): SyncStatus {
    if (syncTime == null || stream == null) return SyncStatus.NOT_SYNCED
    val startTime = stream.startTime ?: return SyncStatus.NOT_SYNCED
    val endTime = stream.endTime

    return when {
        syncTime < startTime -> SyncStatus.WAITING
        // ライブストリーム（endTime == null）または アーカイブ範囲内
        endTime == null || syncTime <= endTime -> SyncStatus.READY
        // アーカイブ終了後
        else -> SyncStatus.WAITING
    }
}
```

### 4.4 UI修正（既存コンポーネント）

#### TimelineCard.kt

```kotlin
when (syncStatus) {
    SyncStatus.READY -> {
        Button(onClick = { /* Story 4で実装 */ }) {
            Icon(Icons.Default.OpenInNew, contentDescription = null)
            Text("Open")
        }
    }
    SyncStatus.WAITING -> {
        Button(
            onClick = { },
            enabled = false
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Text("Wait")
        }
    }
    SyncStatus.NOT_SYNCED -> {
        // ボタンなし
    }
}
```

#### TimelineBar.kt

```kotlin
val barColor = when (syncStatus) {
    SyncStatus.READY -> MaterialTheme.colorScheme.primary
    SyncStatus.WAITING -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    SyncStatus.NOT_SYNCED -> Color.Transparent
}
```

---

## 5. テスト要件

### 5.1 単体テスト

テスト骨格は `SyncTimeViewModelTest.kt` を参照してください。

#### カバレッジ目標

- **ViewModel**: 80%以上
- **SyncStatus判定ロジック**: 100%
- **targetSeekPosition計算ロジック**: 100%

#### 主要テストケース

1. **画面初期化時**
   - syncTimeが一番上のチャンネルのアーカイブ開始時刻に初期化される
   - syncTimeRangeが全ストリームの範囲から計算される

2. **同期時刻更新時**
   - syncTimeが正しく更新される
   - SYNC TIME表示が更新される

3. **スクロール停止時**
   - 全チャンネルのSyncStatusが再計算される
   - 全チャンネルのtargetSeekPositionが計算される

4. **SyncStatus判定ロジック**
   - ストリーム未選択 → NOT_SYNCED
   - syncTime < stream.startTime → WAITING
   - stream.startTime <= syncTime <= stream.endTime → READY
   - syncTime > stream.endTime → WAITING（境界テスト）

5. **targetSeekPosition計算ロジック**
   - 正の再生位置が正しく計算される
   - syncTime == stream.startTime → 0.0f
   - syncTime < stream.startTime → null
   - Float型、秒単位

6. **複数チャンネル**
   - 各チャンネルが独立してSyncStatusを持つ
   - 各チャンネルが独立してtargetSeekPositionを持つ

### 5.2 手動テスト

1. **スクロール操作**
   - タイムラインバーを左右にスクロール
   - SYNC TIME表示がリアルタイム更新されることを確認

2. **状態遷移確認**
   - スクロール停止後、Open/Waitボタンが正しく表示されることを確認
   - WAITING状態のチャンネルがグレーアウトされることを確認
   - READY状態のチャンネルが通常色で表示されることを確認

3. **初期化確認**
   - 画面を開いた時、syncTimeが一番上のチャンネルのアーカイブ開始時刻になることを確認

4. **境界値確認**
   - syncTimeRangeの最初と最後にスクロールして、状態が正しく更新されることを確認

---

## 6. 依存関係

### 6.1 依存するStory

- ✅ Story 1: タイムライン基本表示（完了）
- ✅ Story 2: チャンネル追加・管理（#46、完了）

### 6.2 依存されるStory

- ⏳ Story 4: 外部アプリで開く（#53の一部、Phase 2で実装予定）

---

## 7. 制約事項

### 7.1 既存実装の活用

- **スクロール実装**: `TimelineCardsWithSyncLine.kt`の`scrollable`を活用（新規実装不要）
- **syncTime更新**: `UpdateSyncTime` Intentは既に実装済み
- **isDragging管理**: `StartDragging`/`StopDragging`は既に実装済み
- **syncTimeRange計算**: UiStateの計算済みプロパティを活用

### 7.2 Phase 2で実装する内容

- ViewModel拡張（loadScreen、updateSyncTime、新規メソッド）
- UI修正（TimelineCard、TimelineBar）
- テスト実装（SyncTimeViewModelTest）

### 7.3 対象外

- **外部アプリ起動**: Story 4で実装
- **サーバーサイド同期**: 不要（クライアントサイドのみ）
- **リアルタイム通信**: 不要（手動同期方式）

---

## 8. 参考資料

- **Epic Issue**: #32 Timeline Sync
- **Story Issue**: #53
- **ADR**: `.claude/rules/architecture/004-manual-sync.md`
- **テンプレート**: `docs/design-doc/template/requirements-template.md`
