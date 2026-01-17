package org.example.project.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * アプリ全体で使用するシェイプ（角丸）のデザイントークン
 *
 * 統一感のあるUIを実現するため、直接RoundedCornerShapeを作成せず
 * このオブジェクトで定義された値を使用すること。
 */
object AppShapes {
    /** 小（4dp）- タイムラインバー、プログレスインジケーター等 */
    val small = RoundedCornerShape(4.dp)

    /** 中（8dp）- チップ、検索結果項目等 */
    val medium = RoundedCornerShape(8.dp)

    /** 大（12dp）- カード、ボトムシート等 */
    val large = RoundedCornerShape(12.dp)
}
