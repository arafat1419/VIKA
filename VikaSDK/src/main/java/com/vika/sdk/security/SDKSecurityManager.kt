package com.vika.sdk.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest

/**
 * Security manager to prevent SDK abuse and unauthorized usage.
 *
 * Provides:
 * - App signature verification
 * - Package name validation
 * - Rate limiting
 * - Tamper detection
 */
internal class SDKSecurityManager(private val context: Context) {

    private val requestTimestamps = mutableListOf<Long>()
    private var cachedAppSignature: String? = null

    companion object {
        /** Maximum requests per minute */
        const val MAX_REQUESTS_PER_MINUTE = 60

        /** Window for rate limiting (1 minute) */
        const val RATE_LIMIT_WINDOW_MS = 60_000L
    }

    /**
     * Get the calling app's signature hash.
     *
     * This can be used to verify the app is legitimate and not a tampered version.
     *
     * @return SHA-256 hash of app's signing certificate
     */
    fun getAppSignature(): String {
        cachedAppSignature?.let { return it }

        val signature = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            signatures?.firstOrNull()?.let { sig ->
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(sig.toByteArray())
                Base64.encodeToString(hash, Base64.NO_WRAP)
            } ?: ""
        } catch (e: Exception) {
            ""
        }

        cachedAppSignature = signature
        return signature
    }

    /**
     * Get the app's package name.
     *
     * @return Package name
     */
    fun getPackageName(): String {
        return context.packageName
    }

    /**
     * Check if request should be rate limited.
     *
     * @return True if request should be blocked due to rate limiting
     */
    @Synchronized
    fun isRateLimited(): Boolean {
        val now = System.currentTimeMillis()

        // Remove old timestamps outside the window
        requestTimestamps.removeAll { it < now - RATE_LIMIT_WINDOW_MS }

        // Check if over limit
        if (requestTimestamps.size >= MAX_REQUESTS_PER_MINUTE) {
            return true
        }

        // Record this request
        requestTimestamps.add(now)
        return false
    }

    /**
     * Reset rate limiting counters.
     */
    @Synchronized
    fun resetRateLimit() {
        requestTimestamps.clear()
    }

    /**
     * Verify app integrity using signature.
     *
     * @param expectedSignature Expected app signature hash
     * @return True if signature matches
     */
    fun verifyAppSignature(expectedSignature: String): Boolean {
        return getAppSignature() == expectedSignature
    }

    /**
     * Check if running in debug mode (potential security risk).
     *
     * @return True if app is debuggable
     */
    fun isDebuggable(): Boolean {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, 0
            )
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if running on emulator (potential security risk).
     *
     * @return True if likely running on emulator
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * Get device fingerprint for tracking/validation.
     *
     * @return Device fingerprint string
     */
    fun getDeviceFingerprint(): String {
        val data =
            "${Build.BOARD}|${Build.BRAND}|${Build.DEVICE}|${Build.MANUFACTURER}|${Build.MODEL}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    /**
     * Generate security metadata for API requests.
     *
     * @return Map of security metadata
     */
    fun getSecurityMetadata(): Map<String, String> {
        return mapOf(
            "app_signature" to getAppSignature(),
            "package_name" to getPackageName(),
            "device_fingerprint" to getDeviceFingerprint(),
            "is_debuggable" to isDebuggable().toString(),
            "is_emulator" to isEmulator().toString(),
            "sdk_version" to Build.VERSION.SDK_INT.toString()
        )
    }
}
