package com.vika.sdk.network.exceptions

/**
 * Exception for security-related failures.
 *
 * Thrown when security validation fails, such as:
 * - Missing or invalid response signatures
 * - Invalid content types
 * - Timestamp validation failures
 * - Encryption/decryption errors
 *
 * @param message Error description
 * @param cause Original exception, if any
 */
internal class SecurityException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
