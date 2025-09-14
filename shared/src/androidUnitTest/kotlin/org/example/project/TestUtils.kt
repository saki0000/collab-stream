package org.example.project

import kotlinx.coroutines.runBlocking

/**
 * Android implementation of runTest using runBlocking.
 */
actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }
