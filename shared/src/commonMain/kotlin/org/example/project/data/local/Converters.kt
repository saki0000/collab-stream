package org.example.project.data.local

import androidx.room.TypeConverter
import org.example.project.domain.model.VideoServiceType

/**
 * RoomデータベースのTypeConverter。
 *
 * ドメインモデルの列挙型などをRoomで保存可能な基本型に変換する。
 *
 * Story Issue: #36
 * Epic: EPIC-003（同期チャンネル履歴保存）
 */
class Converters {
    /**
     * VideoServiceTypeをStringに変換する。
     *
     * @param value 変換元のVideoServiceType
     * @return 列挙型の名前文字列
     */
    @TypeConverter
    fun fromServiceType(value: VideoServiceType): String = value.name

    /**
     * StringをVideoServiceTypeに変換する。
     *
     * @param value 変換元の文字列
     * @return 対応するVideoServiceType
     * @throws IllegalStateException データベースに不正な値が存在する場合
     */
    @TypeConverter
    fun toServiceType(value: String): VideoServiceType {
        return try {
            VideoServiceType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // 不正な値がDBに存在する場合、より詳細な例外をスローして問題を検知しやすくする
            throw IllegalStateException("データベースに不明なVideoServiceType値が存在します: '$value'", e)
        }
    }
}
