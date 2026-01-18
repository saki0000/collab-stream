package org.example.project.core.theme

import androidx.compose.ui.unit.dp

/**
 * アプリ全体で使用するスペーシング（間隔）のデザイントークン
 *
 * 一貫性のあるレイアウトを実現するため、直接dp値を使用せず
 * このオブジェクトで定義された値を使用すること。
 */
object Spacing {
    /** 極小（2dp）- 線幅、最小マージン等 */
    val xxs = 2.dp

    /** 小（4dp）- 密接な要素間の内部間隔 */
    val xs = 4.dp

    /** 小間隔（8dp）- 関連する要素間の間隔 */
    val sm = 8.dp

    /** 中間隔（12dp）- セクション内の要素間隔 */
    val md = 12.dp

    /** 大間隔（16dp）- 標準パディング、セクション間隔 */
    val lg = 16.dp

    /** 特大間隔（24dp）- 主要セクション間の間隔 */
    val xl = 24.dp

    /** 最大間隔（32dp）- 大きなセクション区切り */
    val xxl = 32.dp
}
