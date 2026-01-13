package org.example.project.testing.util

import kotlinx.coroutines.runBlocking

/**
 * Android実装: runBlockingを使用してサスペンド関数をテスト。
 */
actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }
