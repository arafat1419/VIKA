package com.vika.sdk.models

/**
 * Result wrapper for SDK operations.
 *
 * Provides a type-safe way to handle success and failure cases
 * with specific error types.
 *
 * ## Usage Example
 * ```kotlin
 * val result = VikaSDK.getInstance().registerScreensAsync(screens)
 * result
 *     .onSuccess { count -> println("Registered $count screens") }
 *     .onFailure { error -> println("Failed: ${error.message}") }
 * ```
 *
 * @param T Type of successful result data
 * @see VikaError
 */
sealed class VikaResult<out T> {
    /**
     * Successful result with data.
     *
     * @property data Result data
     */
    data class Success<T>(val data: T) : VikaResult<T>()

    /**
     * Failed result with error.
     *
     * @property error Error that occurred
     */
    data class Failure(val error: VikaError) : VikaResult<Nothing>()

    /**
     * Check if result is successful.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Check if result is failure.
     */
    val isFailure: Boolean get() = this is Failure

    /**
     * Get data if successful, null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    /**
     * Get error if failure, null otherwise.
     */
    fun errorOrNull(): VikaError? = when (this) {
        is Success -> null
        is Failure -> error
    }

    /**
     * Get data if successful, default value otherwise.
     *
     * @param default Default value to return on failure
     */
    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Failure -> default
    }

    /**
     * Transform successful result data.
     *
     * @param transform Transformation function
     * @return Transformed result
     */
    inline fun <R> map(transform: (T) -> R): VikaResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> this
    }

    /**
     * Execute action on success.
     *
     * @param action Action to execute
     * @return This result
     */
    inline fun onSuccess(action: (T) -> Unit): VikaResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Execute action on failure.
     *
     * @param action Action to execute
     * @return This result
     */
    inline fun onFailure(action: (VikaError) -> Unit): VikaResult<T> {
        if (this is Failure) action(error)
        return this
    }

    companion object {
        /**
         * Create a success result.
         */
        fun <T> success(data: T): VikaResult<T> = Success(data)

        /**
         * Create a failure result.
         */
        fun failure(error: VikaError): VikaResult<Nothing> = Failure(error)
    }
}
