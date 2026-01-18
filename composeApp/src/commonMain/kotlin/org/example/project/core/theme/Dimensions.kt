package org.example.project.core.theme

import androidx.compose.ui.unit.dp

/**
 * アプリ全体で使用するディメンション（サイズ）のデザイントークン
 *
 * アイコンやボタンなど、固定サイズを持つ要素のサイズを定義。
 * 一貫性のあるUIを実現するため、このオブジェクトで定義された値を使用すること。
 */
object Dimensions {
    // === アイコンサイズ ===

    /** 極小アイコン（14dp）- SubStreamItem等の極小アイコン */
    val iconXxs = 14.dp

    /** 最小アイコン（16dp）- TimelineCard等の小アイコン */
    val iconXs = 16.dp

    /** 小アイコン（18dp）- SyncFloatingBar等 */
    val iconSm = 18.dp

    /** 標準アイコン（20dp）- リスト項目内の補助アイコン */
    val iconMd = 20.dp

    /** 大アイコン（24dp）- 通常のアイコンボタン、ツールバーアイコン */
    val iconLg = 24.dp

    /** 特大アイコン（32dp）- プラットフォームアイコン */
    val iconXl = 32.dp

    /** 超特大アイコン（40dp）- FAB、主要アクションアイコン */
    val icon2xl = 40.dp

    /** 最大アイコン（48dp）- 機能アイコン、タブアイコン */
    val icon3xl = 48.dp

    /** 超最大アイコン（64dp）- EmptyState、ErrorStateの中央アイコン */
    val icon4xl = 64.dp

    // === アバターサイズ ===

    /** 小アバター（40dp）- カレンダーセル、小アバター */
    val avatarSm = 40.dp

    /** 標準アバター（56dp）- チャンネルアバター */
    val avatarMd = 56.dp

    // === サムネイルサイズ ===

    /** 小サムネイル幅（80dp）- SubStreamItem */
    val thumbnailSmWidth = 80.dp

    /** 小サムネイル高さ（45dp）- SubStreamItem */
    val thumbnailSmHeight = 45.dp

    /** 標準サムネイル幅（120dp）- SearchResultItem */
    val thumbnailMdWidth = 120.dp

    /** 標準サムネイル高さ（68dp）- SearchResultItem */
    val thumbnailMdHeight = 68.dp

    // === ボタンサイズ ===

    /** 標準ボタン高さ（48dp）- タッチターゲットサイズを考慮 */
    val buttonHeight = 48.dp

    /** ボタン最小幅（120dp）- 読みやすさを保証 */
    val buttonMinWidth = 120.dp
}
