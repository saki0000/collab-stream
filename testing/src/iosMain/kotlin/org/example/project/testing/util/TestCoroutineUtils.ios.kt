package org.example.project.testing.util

import kotlinx.coroutines.runBlocking

/**
 * iOS実装: runBlockingを使用してサスペンド関数をテスト。
 */
actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }
