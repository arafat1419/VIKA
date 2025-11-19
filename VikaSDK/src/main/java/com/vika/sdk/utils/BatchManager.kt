package com.vika.sdk.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Generic batch manager for grouping multiple operations into batches.
 *
 * Collects items and processes them in batches after a delay or when
 * the batch size is reached.
 *
 * @param T Type of items to batch
 * @param scope Coroutine scope for batch processing
 * @param batchSize Maximum items per batch
 * @param delayMs Delay before processing incomplete batch
 * @param processor Function to process a batch of items
 */
internal class BatchManager<T>(
    private val scope: CoroutineScope,
    private val batchSize: Int = DEFAULT_BATCH_SIZE,
    private val delayMs: Long = DEFAULT_DELAY_MS,
    private val processor: suspend (List<T>) -> Unit
) {
    private val items = mutableListOf<T>()
    private val mutex = Mutex()
    private var batchJob: Job? = null

    companion object {
        /** Default batch size */
        const val DEFAULT_BATCH_SIZE = 50

        /** Default delay before processing incomplete batch */
        const val DEFAULT_DELAY_MS = 500L
    }

    /**
     * Add an item to the batch queue.
     *
     * @param item Item to add
     */
    suspend fun add(item: T) {
        mutex.withLock {
            items.add(item)

            if (items.size >= batchSize) {
                processBatch()
            } else {
                scheduleBatch()
            }
        }
    }

    /**
     * Add multiple items to the batch queue.
     *
     * @param newItems Items to add
     */
    suspend fun addAll(newItems: List<T>) {
        mutex.withLock {
            items.addAll(newItems)

            // Process in batches if over size
            while (items.size >= batchSize) {
                val batch = items.take(batchSize)
                items.removeAll(batch.toSet())
                processor(batch)
            }

            // Schedule remaining items
            if (items.isNotEmpty()) {
                scheduleBatch()
            }
        }
    }

    /**
     * Force process any pending items immediately.
     */
    suspend fun flush() {
        mutex.withLock {
            batchJob?.cancel()
            batchJob = null

            if (items.isNotEmpty()) {
                val batch = items.toList()
                items.clear()
                processor(batch)
            }
        }
    }

    /**
     * Get count of pending items.
     *
     * @return Number of items waiting to be processed
     */
    suspend fun pendingCount(): Int {
        return mutex.withLock { items.size }
    }

    /**
     * Clear all pending items without processing.
     */
    suspend fun clear() {
        mutex.withLock {
            batchJob?.cancel()
            batchJob = null
            items.clear()
        }
    }

    private fun scheduleBatch() {
        batchJob?.cancel()
        batchJob = scope.launch {
            delay(delayMs)
            mutex.withLock {
                if (items.isNotEmpty()) {
                    processBatch()
                }
            }
        }
    }

    private suspend fun processBatch() {
        batchJob?.cancel()
        batchJob = null

        val batch = items.toList()
        items.clear()
        processor(batch)
    }
}
