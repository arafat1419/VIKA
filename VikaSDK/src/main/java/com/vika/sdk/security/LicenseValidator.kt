package com.vika.sdk.security

/**
 * License validation for SDK usage.
 *
 * Validates that the API key is authorized for the current app
 * and prevents unauthorized usage or key sharing.
 */
internal class LicenseValidator(
    private val securityManager: SDKSecurityManager
) {

    /**
     * Validation result with status and message.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorCode: ErrorCode? = null,
        val message: String? = null
    )

    /**
     * Error codes for license validation failures.
     */
    enum class ErrorCode {
        /** API key is empty or malformed */
        INVALID_API_KEY,

        /** API key not authorized for this package */
        UNAUTHORIZED_PACKAGE,

        /** App signature mismatch */
        SIGNATURE_MISMATCH,

        /** Too many requests */
        RATE_LIMITED,

        /** License expired */
        LICENSE_EXPIRED,

        /** Running on unauthorized device */
        UNAUTHORIZED_DEVICE
    }

    /**
     * Validate API key format.
     *
     * @param apiKey API key to validate
     * @return True if format is valid
     */
    fun isValidApiKeyFormat(apiKey: String): Boolean {
        // Basic format validation
        if (apiKey.isBlank()) return false
        if (apiKey.length < 16) return false
        // Add more format checks as needed
        return true
    }

    /**
     * Perform comprehensive license validation.
     *
     * Checks:
     * - API key format
     * - Rate limiting
     * - Device/environment checks
     *
     * @param apiKey API key to validate
     * @param allowDebug Whether to allow debuggable apps
     * @param allowEmulator Whether to allow emulator usage
     * @return Validation result
     */
    fun validate(
        apiKey: String,
        allowDebug: Boolean = true,
        allowEmulator: Boolean = true
    ): ValidationResult {
        // Check API key format
        if (!isValidApiKeyFormat(apiKey)) {
            return ValidationResult(
                isValid = false,
                errorCode = ErrorCode.INVALID_API_KEY,
                message = "Invalid API key format"
            )
        }

        // Check rate limiting
        if (securityManager.isRateLimited()) {
            return ValidationResult(
                isValid = false,
                errorCode = ErrorCode.RATE_LIMITED,
                message = "Too many requests. Please wait before trying again."
            )
        }

        // Check debug mode
        if (!allowDebug && securityManager.isDebuggable()) {
            return ValidationResult(
                isValid = false,
                errorCode = ErrorCode.UNAUTHORIZED_DEVICE,
                message = "SDK cannot run in debug mode for this license"
            )
        }

        // Check emulator
        if (!allowEmulator && securityManager.isEmulator()) {
            return ValidationResult(
                isValid = false,
                errorCode = ErrorCode.UNAUTHORIZED_DEVICE,
                message = "SDK cannot run on emulator for this license"
            )
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Validate server response for license.
     *
     * Call after server validates the API key and returns authorized package names.
     *
     * @param authorizedPackages List of package names authorized for this API key
     * @return Validation result
     */
    fun validatePackageAuthorization(authorizedPackages: List<String>): ValidationResult {
        val currentPackage = securityManager.getPackageName()

        if (currentPackage !in authorizedPackages) {
            return ValidationResult(
                isValid = false,
                errorCode = ErrorCode.UNAUTHORIZED_PACKAGE,
                message = "Package '$currentPackage' is not authorized for this API key"
            )
        }

        return ValidationResult(isValid = true)
    }

    /**
     * Validate app signature against expected value.
     *
     * @param expectedSignature Expected signature hash from server
     * @return Validation result
     */
    fun validateSignature(expectedSignature: String): ValidationResult {
        if (!securityManager.verifyAppSignature(expectedSignature)) {
            return ValidationResult(
                isValid = false,
                errorCode = ErrorCode.SIGNATURE_MISMATCH,
                message = "App signature does not match. Possible tampering detected."
            )
        }

        return ValidationResult(isValid = true)
    }
}
