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

    /** 小アイコン（20dp）- リスト項目内の補助アイコン */
    val iconSmall = 20.dp

    /** 標準アイコン（24dp）- 通常のアイコンボタン、ツールバーアイコン */
    val iconMedium = 24.dp

    /** 大アイコン（40dp）- FAB、主要アクションアイコン */
    val iconLarge = 40.dp

    /** 特大アイコン（48dp）- 機能アイコン、タブアイコン */
    val iconXLarge = 48.dp

    /** 最大アイコン（64dp）- EmptyState、ErrorStateの中央アイコン */
    val iconXXLarge = 64.dp

    // === ボタンサイズ ===

    /** 標準ボタン高さ（48dp）- タッチターゲットサイズを考慮 */
    val buttonHeight = 48.dp

    /** ボタン最小幅（120dp）- 読みやすさを保証 */
    val buttonMinWidth = 120.dp
}
