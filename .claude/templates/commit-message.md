# コミットメッセージテンプレート

## 形式

```
<type>: <subject>

<body>

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```

## Type

| Type | 用途 |
|------|------|
| feat | 新機能追加 |
| fix | バグ修正 |
| refactor | リファクタリング（機能変更なし） |
| docs | ドキュメントのみの変更 |
| test | テストの追加・修正 |
| chore | ビルド設定、依存関係の更新 |
| style | コードスタイルの変更（フォーマット等） |

## Subject

- 日本語で簡潔に（50文字以内）
- 動詞で始める（「追加」「修正」「変更」等）
- 末尾にピリオドを付けない

## Body（任意）

- 変更の理由や背景を説明
- 空行で Subject と区切る
- 72文字で折り返し

## 例

```
feat: 動画同期ボタンを追加

ユーザーが2つの動画を同期再生できるようにするため、
メインUIに同期ボタンを追加。

- SyncButton コンポーネントを新規作成
- VideoSyncViewModel に同期処理を追加
- 同期状態を UiState で管理

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```

```
fix: 動画再生時のクラッシュを修正

再生位置が動画の長さを超えた場合に発生していた
IndexOutOfBoundsException を修正。

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```

```
refactor: VideoRepository の DI 設定を改善

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
```
