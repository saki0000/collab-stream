package org.example.project.testing.util

/**
 * サスペンド関数をテストするためのユーティリティ。
 *
 * プラットフォーム固有の実装で適切なコルーチンコンテキストを提供。
 */
expect fun runTest(block: suspend () -> Unit)
