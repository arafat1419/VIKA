package com.vika.sdk.network.exceptions

/**
 * Exception for network-related failures.
 *
 * Thrown when network operations fail, such as:
 * - Connection failures
 * - Request timeouts
 * - Invalid responses
 * - Retry exhaustion
 *
 * @param message Error description
 * @param cause Original exception, if any
 */
internal class NetworkException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
