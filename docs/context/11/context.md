# Context Management - Issue #11

## 📋 Issue Overview

**Title**: [Feature] YouTube再生時間取得機能
**Type**: feature
**Labels**: feature
**Status**: In Progress

## 🎯 機能概要

埋め込みYouTube動画の現在の再生位置（秒）と配信開始時間を元に、実際の日時に変換して取得する機能を実装します。この機能は、複数の動画を時刻で同期視聴するための基盤機能として位置づけられています。

**対象プラットフォーム**: Android ✅, iOS ✅
**対象外**: Web (WASM) ❌, Server ❌

## 🏗️ アーキテクチャ概要

```
VideoSyncController → VideoSyncUseCase → VideoSyncRepository
                   ↘ PlaybackPositionProvider
```

## 📐 技術スタック

- **共通ロジック**: Kotlin Multiplatform (shared)
- **UI**: Compose Multiplatform (composeApp)
- **API**: YouTube Data API v3 (クライアント直接呼び出し)
- **プラットフォーム固有**: expect/actual パターン

## 🎖️ 成功指標

- **精度**: 時刻計算精度1秒以内
- **パフォーマンス**: API取得から結果表示まで3秒以内
- **品質**: API成功率95%以上

## 🔍 実装方針

1. **Interface First**: Design Docで定義されたInterfaceを基に実装
2. **Layer順序**: shared → composeApp の順で依存関係に従って実装
3. **品質確保**: 各フェーズで単体テストとビルド確認
4. **プラットフォーム対応**: 既存VideoPlayerViewの拡張

---

**Created**: 2025-01-14
**Updated**: 2025-01-14
**Phase**: Context Setup