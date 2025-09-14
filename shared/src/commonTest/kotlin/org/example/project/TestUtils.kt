package org.example.project

/**
 * Helper function for running suspending tests.
 * Platform-specific implementations handle coroutine context appropriately.
 */
expect fun runTest(block: suspend () -> Unit)