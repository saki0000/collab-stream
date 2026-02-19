package org.example.project.domain.model

/**
 * コメントテキストからタイムスタンプを抽出するユーティリティ。
 *
 * Epic: コメントタイムスタンプ同期
 * US-2: タイムスタンプ抽出とマーカー生成
 */
object TimestampExtractor {

    /**
     * 正規表現パターン: M:SS, MM:SS, H:MM:SS, HH:MM:SS を抽出
     *
     * パターン詳細:
     * - `(?<!\d)`: 負の後読み（直前に数字がないことを確認）
     * - `(?<!/)`: 負の後読み（直前にスラッシュがないことを確認、日付除外）
     * - `(\d{1,2})`: 1桁または2桁の数字（時または分）
     * - `:(\d{2})`: コロン + 2桁の秒
     * - `(?::(\d{2}))?`: オプショナルな（コロン + 2桁の分/秒）
     * - `(?!\d)`: 負の先読み（直後に数字がないことを確認）
     */
    private val TIMESTAMP_REGEX = Regex("""(?<!\d)(?<!/)(\d{1,2}):(\d{2})(?::(\d{2}))?(?!\d)""")

    /**
     * テキストからタイムスタンプを抽出する。
     *
     * @param text 検索対象のテキスト
     * @param videoDurationSeconds 動画の長さ（秒）。指定した場合、この長さを超えるタイムスタンプは除外される。
     * @return 抽出されたタイムスタンプのリスト
     */
    fun extractTimestamps(
        text: String,
        videoDurationSeconds: Long? = null,
    ): List<ExtractedTimestamp> {
        return TIMESTAMP_REGEX.findAll(text).mapNotNull { matchResult ->
            val groups = matchResult.groupValues

            // groups[0]: マッチ全体（例: "1:23:45"）
            // groups[1]: 最初の数字（時または分）
            // groups[2]: 2番目の数字（秒または分）
            // groups[3]: 3番目の数字（秒、存在する場合）

            val firstNumber = groups[1].toInt()
            val secondNumber = groups[2].toInt()
            val thirdNumber = groups.getOrNull(3)?.takeIf { it.isNotEmpty() }?.toInt()

            val (hours, minutes, seconds, hasHours) = if (thirdNumber != null) {
                // H:MM:SS または HH:MM:SS 形式
                // 分と秒の範囲チェック（00-59）
                if (secondNumber >= 60 || thirdNumber >= 60) {
                    return@mapNotNull null
                }
                Tuple4(firstNumber, secondNumber, thirdNumber, true)
            } else {
                // M:SS または MM:SS 形式
                // 秒の範囲チェック（00-59）
                if (secondNumber >= 60) {
                    return@mapNotNull null
                }
                Tuple4(0, firstNumber, secondNumber, false)
            }

            val totalSeconds = toSeconds(hours, minutes, seconds)

            // 動画の長さチェック
            if (videoDurationSeconds != null && totalSeconds > videoDurationSeconds) {
                return@mapNotNull null
            }

            ExtractedTimestamp(
                timestampSeconds = totalSeconds,
                displayTimestamp = formatTimestamp(hours, minutes, seconds, hasHours),
            )
        }.toList()
    }

    /**
     * 時、分、秒を合計秒数に変換する。
     *
     * @param hours 時
     * @param minutes 分
     * @param seconds 秒
     * @return 合計秒数
     */
    fun toSeconds(hours: Int, minutes: Int, seconds: Int): Long {
        return (hours * 3600L + minutes * 60L + seconds).toLong()
    }

    /**
     * 時、分、秒を表示用のタイムスタンプ文字列にフォーマットする。
     *
     * @param hours 時
     * @param minutes 分
     * @param seconds 秒
     * @param hasHours 元のフォーマットに時間が含まれているか
     * @return フォーマットされたタイムスタンプ文字列（例: "1:23:45", "5:30"）
     */
    private fun formatTimestamp(hours: Int, minutes: Int, seconds: Int, hasHours: Boolean): String {
        return if (hasHours) {
            // H:MM:SS または HH:MM:SS 形式
            "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            // M:SS または MM:SS 形式
            "$minutes:${seconds.toString().padStart(2, '0')}"
        }
    }

    /**
     * 4要素のタプル（Kotlinには標準で4要素タプルがないため定義）
     */
    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

/**
 * 抽出されたタイムスタンプ情報。
 *
 * @property timestampSeconds 動画内の秒数
 * @property displayTimestamp 表示用のタイムスタンプ文字列（例: "1:23:45"）
 */
data class ExtractedTimestamp(
    val timestampSeconds: Long,
    val displayTimestamp: String,
)
