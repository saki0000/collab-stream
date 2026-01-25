package org.example.project.core.theme

import androidx.compose.ui.unit.dp

/**
 * アプリ全体で使用するエレベーション（影の深さ）のデザイントークン
 *
 * 要素の重要度や階層を視覚的に表現するため、
 * このオブジェクトで定義された値を使用すること。
 */
object Elevation {
    /** なし（0dp）- フラットな要素 */
    val none = 0.dp

    /** 低（2dp）- EmptyState, ErrorState等の軽い浮き上がり */
    val low = 2.dp

    /** 中（4dp）- 標準的なカード */
    val medium = 4.dp

    /** 高（8dp）- フローティングボタン、モーダル等の強調要素 */
    val high = 8.dp
}
