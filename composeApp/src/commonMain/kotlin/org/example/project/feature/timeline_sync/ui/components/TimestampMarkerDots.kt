@file:OptIn(ExperimentalTime::class)

package org.example.project.feature.timeline_sync.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.time.ExperimentalTime
import org.example.project.core.theme.AppTheme
import org.example.project.domain.model.TimestampMarker
import org.example.project.domain.model.VideoComment
import org.jetbrains.compose.ui.tooling.preview.Preview

/** マーカードットのサイズ（dp） */
private val MARKER_DOT_RADIUS_DP = 5.dp

/** タップ領域のサイズ（dp）- ドットより大きなタップ領域を確保 */
private val MARKER_TAP_RADIUS_DP = 16.dp

/** クラスタリングのしきい値（バー幅に対する比率）- 3% 以内を集約 */
private const val CLUSTER_THRESHOLD_FRACTION = 0.03f

/**
 * タイムラインバー上にタイムスタンプマーカードットを表示するコンポーネント。
 *
 * 近接マーカーはクラスタリングして1点に集約し、タップでコールバックを呼び出す。
 * タップ領域はドットより大きい 32dp を確保して操作性を向上させる。
 *
 * バー上の相対位置（0.0〜1.0）を使用してドット描画位置を計算する。
 *
 * Epic: コメントタイムスタンプ同期
 * Story: US-3 (タイムスタンプマーカー表示)
 *
 * @param markers 表示するタイムスタンプマーカーのリスト
 * @param barStartFraction バーの開始位置（タイムレンジ全体に対する割合 0.0〜1.0）
 * @param barEndFraction バーの終了位置（タイムレンジ全体に対する割合 0.0〜1.0）
 * @param onMarkerClick マーカータップ時のコールバック（代表マーカーを引数に渡す）
 * @param modifier Modifier
 */
@Composable
fun TimestampMarkerDots(
    markers: List<TimestampMarker>,
    barStartFraction: Float,
    barEndFraction: Float,
    onMarkerClick: (TimestampMarker) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (markers.isEmpty()) return

    val dotColor = MaterialTheme.colorScheme.tertiary
    val density = LocalDensity.current

    val markerDotRadiusPx = with(density) { MARKER_DOT_RADIUS_DP.toPx() }
    val tapRadiusPx = with(density) { MARKER_TAP_RADIUS_DP.toPx() }

    val barDuration = barEndFraction - barStartFraction
    if (barDuration <= 0f) return

    Box(modifier = modifier) {
        // クラスタリング済みマーカーリストをキャッシュ
        val clusters = remember(markers, barStartFraction, barEndFraction) {
            clusterMarkers(
                markers = markers,
                clusterThreshold = CLUSTER_THRESHOLD_FRACTION,
            )
        }

        // 描画用の位置（バー内の相対位置 0.0〜1.0）を計算してキャッシュ
        // マーカーセット全体の最小・最大秒数を基準にした位置を、バー内位置に変換する
        val clusterBarFractions = remember(clusters, barStartFraction, barEndFraction) {
            clusters.map { cluster ->
                val markerFraction = cluster.centerFraction
                // バー外の場合はクリップ
                ((markerFraction - barStartFraction) / barDuration).coerceIn(0f, 1f)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(clusters, clusterBarFractions) {
                    detectTapGestures { tapOffset ->
                        // タップされた位置に最も近いクラスターを検索
                        val barWidth = size.width.toFloat()
                        val barCenterY = size.height / 2f

                        clusters.forEachIndexed { index, cluster ->
                            val dotX = clusterBarFractions[index] * barWidth
                            val dx = tapOffset.x - dotX
                            val dy = tapOffset.y - barCenterY
                            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                            if (distance <= tapRadiusPx) {
                                onMarkerClick(cluster.representativeMarker)
                                return@detectTapGestures
                            }
                        }
                    }
                },
        ) {
            val barCenterY = size.height / 2f

            clusterBarFractions.forEachIndexed { index, fraction ->
                val dotX = fraction * size.width
                drawCircle(
                    color = dotColor,
                    radius = markerDotRadiusPx,
                    center = Offset(dotX, barCenterY),
                )
            }
        }
    }
}

/**
 * マーカークラスターを表すデータクラス。
 *
 * @param markers このクラスターに属するマーカーリスト
 * @param centerFraction クラスターの中心位置（マーカーセット全体に対する0.0〜1.0）
 * @param representativeMarker タップ時にプレビュー表示する代表マーカー（いいね数最大）
 */
data class MarkerCluster(
    val markers: List<TimestampMarker>,
    val centerFraction: Float,
    val representativeMarker: TimestampMarker,
)

/**
 * タイムスタンプマーカーをバー幅に対してクラスタリングする。
 *
 * 近接マーカー（しきい値 3% 以内）をグループ化し、
 * グループの中央位置に代表ドットを配置する。
 * 代表マーカーはいいね数の最も多いコメントを選択する。
 *
 * @param markers クラスタリング対象のマーカーリスト
 * @param clusterThreshold クラスタリングしきい値（0.0〜1.0）
 * @return クラスタリング済みマーカークラスターのリスト
 */
fun clusterMarkers(
    markers: List<TimestampMarker>,
    clusterThreshold: Float = CLUSTER_THRESHOLD_FRACTION,
): List<MarkerCluster> {
    if (markers.isEmpty()) return emptyList()

    // マーカー全体の時間範囲を計算してバー内相対位置を計算
    val minSeconds = markers.minOf { it.timestampSeconds }.toFloat()
    val maxSeconds = markers.maxOf { it.timestampSeconds }.toFloat()
    val range = (maxSeconds - minSeconds).takeIf { it > 0f } ?: 1f

    // 各マーカーのバー内相対位置を計算
    val markersWithFraction = markers
        .sortedBy { it.timestampSeconds }
        .map { marker ->
            val fraction = (marker.timestampSeconds - minSeconds) / range
            marker to fraction
        }

    // クラスタリング（隣接マーカーをしきい値以内でまとめる）
    val clusters = mutableListOf<MarkerCluster>()
    var currentCluster = mutableListOf(markersWithFraction.first())

    for (i in 1 until markersWithFraction.size) {
        val (_, currentFraction) = markersWithFraction[i]
        val (_, lastFraction) = currentCluster.last()

        if (currentFraction - lastFraction <= clusterThreshold) {
            // 同じクラスターに追加
            currentCluster.add(markersWithFraction[i])
        } else {
            // 現在のクラスターを確定して新しいクラスターを開始
            clusters.add(buildCluster(currentCluster.map { it.first }, currentCluster.map { it.second }))
            currentCluster = mutableListOf(markersWithFraction[i])
        }
    }
    // 最後のクラスターを追加
    clusters.add(buildCluster(currentCluster.map { it.first }, currentCluster.map { it.second }))

    return clusters
}

/**
 * クラスターを構築するヘルパー関数。
 *
 * @param clusterMarkers クラスターに属するマーカーリスト
 * @param fractions 各マーカーのバー内相対位置リスト
 * @return 構築したクラスター
 */
private fun buildCluster(
    clusterMarkers: List<TimestampMarker>,
    fractions: List<Float>,
): MarkerCluster {
    val centerFraction = fractions.average().toFloat()
    val representativeMarker = clusterMarkers.maxByOrNull { it.comment.likeCount }
        ?: clusterMarkers.first()
    return MarkerCluster(
        markers = clusterMarkers,
        centerFraction = centerFraction,
        representativeMarker = representativeMarker,
    )
}

// ============================================
// Previews
// ============================================

@Preview
@Composable
private fun TimestampMarkerDotsPreview() {
    val mockMarkers = listOf(
        TimestampMarker(
            timestampSeconds = 600L,
            displayTimestamp = "10:00",
            comment = VideoComment(
                commentId = "c1",
                authorDisplayName = "ユーザーA",
                authorProfileImageUrl = "",
                textContent = "10:00 ここが面白い",
                likeCount = 120,
                publishedAt = "2024-01-01T10:00:00Z",
            ),
        ),
        TimestampMarker(
            timestampSeconds = 1800L,
            displayTimestamp = "30:00",
            comment = VideoComment(
                commentId = "c2",
                authorDisplayName = "ユーザーB",
                authorProfileImageUrl = "",
                textContent = "30:00 クライマックス！",
                likeCount = 350,
                publishedAt = "2024-01-01T10:00:00Z",
            ),
        ),
        TimestampMarker(
            timestampSeconds = 1830L,
            displayTimestamp = "30:30",
            comment = VideoComment(
                commentId = "c3",
                authorDisplayName = "ユーザーC",
                authorProfileImageUrl = "",
                textContent = "30:30 最高の場面",
                likeCount = 200,
                publishedAt = "2024-01-01T10:00:00Z",
            ),
        ),
        TimestampMarker(
            timestampSeconds = 3600L,
            displayTimestamp = "1:00:00",
            comment = VideoComment(
                commentId = "c4",
                authorDisplayName = "ユーザーD",
                authorProfileImageUrl = "",
                textContent = "1:00:00 1時間経過",
                likeCount = 80,
                publishedAt = "2024-01-01T10:00:00Z",
            ),
        ),
    )

    AppTheme {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            TimestampMarkerDots(
                markers = mockMarkers,
                barStartFraction = 0.0f,
                barEndFraction = 1.0f,
                onMarkerClick = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun TimestampMarkerDotsEmptyPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .width(300.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            TimestampMarkerDots(
                markers = emptyList(),
                barStartFraction = 0.0f,
                barEndFraction = 1.0f,
                onMarkerClick = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
