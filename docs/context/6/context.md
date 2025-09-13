# Context for Issue #6: YouTube表示機能

## Issue Information
- **Issue Number**: #6
- **Title**: [Feature] YouTube表示機能
- **Type**: Feature
- **Priority**: Standard

## Overview
YouTube表示機能の実装 - Android/iOS プラットフォームにおけるYouTube動画プレーヤーの統合。expect/actualパターンを使用したプラットフォーム固有実装。

## Key Requirements
- Android: YouTube Android Player API + AndroidView
- iOS: WKWebView + iframe埋め込み + UIKitView
- 共通インターフェース: VideoPlayerView expect/actual
- 状態管理: VideoUiState
- エラーハンドリング: 統一されたエラー処理

## Implementation Scope
- ✅ **Design Doc**: `docs/design-doc/YouTube表示機能.md`
- 🎯 **Target Platforms**: Android, iOS
- ❌ **Excluded**: Web (WASM), 動画同期機能, 複数動画同時再生

## Context Files
- Design Doc Reference: `design-doc-ref.md`
- Interface Implementation: `interface-impl.md`
- Tasks Management: `tasks.md`