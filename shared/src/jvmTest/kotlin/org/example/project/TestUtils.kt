package org.example.project

import kotlinx.coroutines.runBlocking

/**
 * JVM implementation of runTest using runBlocking.
 */
actual fun runTest(block: suspend () -> Unit) = runBlocking { block() }