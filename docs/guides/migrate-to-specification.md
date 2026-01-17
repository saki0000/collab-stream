# REQUIREMENTS.md → SPECIFICATION.md 移行ガイド

## 概要

Phase 1で作成する仕様ファイルが、従来の2ファイル構成から1ファイル統合形式に変更されました。

**従来（非推奨）**:
- `feature/{feature_name}/REQUIREMENTS.md` - 3セクション構成
- `feature/{feature_name}/screen-transition.md` - Mermaid図

**新形式（推奨）**:
- `feature/{feature_name}/SPECIFICATION.md` - 3セクション統合仕様書

## 変更理由

1. **ファイル分散の解消**: 仕様が2ファイルに分かれていた問題を解決
2. **検索性の向上**: 1ファイルで全仕様を確認可能
3. **メンテナンス効率**: 更新時のファイル移動が不要
4. **SSoTの徹底**: Single Source of Truth を1ファイルで実現

## 移行が必要なケース

以下のいずれかに該当する場合、移行を検討してください：

1. **新機能追加時**: `/phase1`コマンドを実行する場合
2. **既存仕様の更新時**: REQUIREMENTS.mdを大幅に更新する場合
3. **任意移行**: 既存機能の整理時（時間がある場合）

## 移行手順

### 1. SPECIFICATION.mdの作成

既存のREQUIREMENTS.mdとscreen-transition.mdをSPECIFICATION.mdに統合します。

```bash
# 機能ディレクトリに移動
cd composeApp/src/commonMain/kotlin/org/example/project/feature/{feature_name}

# テンプレートをコピー
cp ../../../../../../docs/design-doc/template/specification-template.md SPECIFICATION.md
```

### 2. Section 1 & 2のコピー

REQUIREMENTS.mdのSection 1とSection 2をSPECIFICATION.mdにコピーします。

```bash
# REQUIREMENTS.mdのSection 1, 2をコピー
# （手動でコピー、またはエディタで実行）
```

**コピー対象**:
- Section 1: ユーザーストーリー
- Section 2: ビジネスルール

**コピー不要**:
- ~~Section 3: 画面フローと状態遷移~~ → 次のステップで置き換え
- ~~Section 4: Phase 2実装進捗~~ → Story Issueで管理

### 3. Section 3にMermaid図を統合

screen-transition.mdのMermaid図をSPECIFICATION.mdのSection 3に統合します。

**手順**:

1. screen-transition.mdを開く
2. Mermaid図ブロックをコピー（```mermaid ... ``` 部分）
3. SPECIFICATION.mdのSection 3に貼り付け

**SPECIFICATION.mdのSection 3フォーマット**:

```markdown
## 3. 画面内の状態遷移

画面の状態（Loading, Content, Error等）とユーザーアクションによる遷移を定義します。

### 状態遷移図

```mermaid
stateDiagram-v2
    [*] --> 初期化中
    初期化中 --> 読み込み中: 画面が開かれた
    ...
\```

### 関連ドキュメント

- **App Navigation**: [/docs/screen-navigation.md](/docs/screen-navigation.md)
- **Module Navigation**: [/docs/navigation/{module}-module.md](/docs/navigation/{module}-module.md)
```

### 4. メタデータの更新

SPECIFICATION.md下部のメタデータを更新します。

```markdown
---

**作成者**: {Name}
**作成日**: {YYYY-MM-DD}
**関連Issue**: #{Issue Number}
```

### 5. 旧ファイルの削除

SPECIFICATION.mdが完成したら、旧ファイルを削除します。

```bash
# 旧ファイルを削除
git rm REQUIREMENTS.md
git rm screen-transition.md

# （該当する場合）diagram/ディレクトリも削除
git rm -r diagram/

# コミット
git add SPECIFICATION.md
git commit -m "refactor: REQUIREMENTS.md → SPECIFICATION.mdに移行"
```

## 移行例

### 移行前

```
feature/timeline_sync/
├── REQUIREMENTS.md         # 95行
├── screen-transition.md    # 150行
└── ui/
    └── ...
```

**REQUIREMENTS.md (Section 3)**:
```markdown
## 3. 画面フローと状態遷移

### 画面内の振る舞い（Level 3）
- **Screen Transition**: [screen-transition.md](./screen-transition.md)
```

### 移行後

```
feature/timeline_sync/
├── SPECIFICATION.md        # 統合仕様書（約180行）
└── ui/
    └── ...
```

**SPECIFICATION.md (Section 3)**:
```markdown
## 3. 画面内の状態遷移

画面の状態（Loading, Content, Error等）とユーザーアクションによる遷移を定義します。

### 状態遷移図

```mermaid
stateDiagram-v2
    [*] --> 初期化中
    初期化中 --> 検索待機: 初期化完了
    ...
\```

### 関連ドキュメント
- **App Navigation**: [/docs/screen-navigation.md](/docs/screen-navigation.md)
```

## チェックリスト

移行作業の完了条件：

- [ ] SPECIFICATION.md作成完了
- [ ] Section 1: ユーザーストーリーをコピー
- [ ] Section 2: ビジネスルールをコピー
- [ ] Section 3: Mermaid図を統合
- [ ] 関連ドキュメントリンクを追加
- [ ] メタデータを更新
- [ ] REQUIREMENTS.mdを削除（git rm）
- [ ] screen-transition.mdを削除（git rm）
- [ ] コミット完了

## FAQ

### Q1: 既存のREQUIREMENTS.mdはいつまで使える？

A: **互換性は維持されます**。Phase 1コマンドは既存ファイルを検索し、見つかった場合は更新します。ただし、新機能ではSPECIFICATION.mdを推奨します。

### Q2: すべての機能を一度に移行すべき？

A: **いいえ**。段階的移行で問題ありません。新機能や大幅更新時に移行すれば十分です。

### Q3: 移行後にPhase 2コマンドは動作する？

A: **はい**。Phase 2コマンドはSPECIFICATION.mdとREQUIREMENTS.mdの両方をサポートします。

### Q4: screen-transition.mdは完全に不要？

A: **はい**。SPECIFICATION.mdのSection 3に統合されたため、別ファイルは不要です。

### Q5: ViewModelTestの参照も更新すべき？

A: **はい**。KDocの参照をSPECIFICATION.mdに更新してください。

```kotlin
/**
 * タイムライン同期画面の振る舞い仕様
 * Specification: feature/timeline_sync/SPECIFICATION.md  // ← 更新
 * Story Issue: #123
 */
```

## 参照

- **新しいテンプレート**: `docs/design-doc/template/specification-template.md`
- **Phase 1コマンド**: `.claude/commands/phase1.md`
- **ドキュメント規約**: `.claude/rules/documentation.md`

---

**最終更新**: 2026-01-17
**バージョン**: 1.0
