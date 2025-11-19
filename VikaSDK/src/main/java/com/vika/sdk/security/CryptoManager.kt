package com.vika.sdk.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic operations manager for secure data encryption and decryption.
 *
 * Uses AES-256-CBC with PBKDF2 key derivation and random IV generation
 * for industry-standard security.
 *
 * @param apiKey API key used for key derivation
 * @param salt Salt for PBKDF2 (should be unique per device/installation)
 */
internal class CryptoManager(
    apiKey: String,
    salt: ByteArray = DEFAULT_SALT
) {
    private val encryptionKey: ByteArray
    private val secureRandom = SecureRandom()

    companion object {
        private const val ALGORITHM = "AES/CBC/PKCS5Padding"
        private const val KEY_ALGORITHM = "AES"
        private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val KEY_LENGTH_BITS = 256
        private const val IV_LENGTH_BYTES = 16
        private const val PBKDF2_ITERATIONS = 10000

        // Default salt - in production, use device-specific salt
        private val DEFAULT_SALT = "VikaSDK2024Salt".toByteArray()
    }

    init {
        encryptionKey = deriveKey(apiKey, salt)
    }

    /**
     * Encrypt data using AES-256-CBC with random IV.
     *
     * The IV is prepended to the encrypted data for use during decryption.
     *
     * @param plaintext Data to encrypt
     * @return Base64-encoded encrypted data with prepended IV
     * @throws SecurityException if encryption fails
     */
    fun encrypt(plaintext: String): String {
        try {
            val iv = generateRandomIV()
            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = SecretKeySpec(encryptionKey, KEY_ALGORITHM)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            // Prepend IV to encrypted data
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)

            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw CryptoSecurityException("Encryption failed", e)
        }
    }

    /**
     * Decrypt data encrypted with [encrypt].
     *
     * Extracts the IV from the beginning of the encrypted data.
     *
     * @param encryptedData Base64-encoded encrypted data with prepended IV
     * @return Decrypted plaintext
     * @throws SecurityException if decryption fails
     */
    fun decrypt(encryptedData: String): String {
        try {
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)

            // Extract IV from beginning
            val iv = combined.copyOfRange(0, IV_LENGTH_BYTES)
            val encrypted = combined.copyOfRange(IV_LENGTH_BYTES, combined.size)

            val cipher = Cipher.getInstance(ALGORITHM)
            val keySpec = SecretKeySpec(encryptionKey, KEY_ALGORITHM)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decrypted = cipher.doFinal(encrypted)

            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            throw CryptoSecurityException("Decryption failed", e)
        }
    }

    /**
     * Generate a cryptographically secure random IV.
     *
     * @return 16-byte random IV
     */
    private fun generateRandomIV(): ByteArray {
        val iv = ByteArray(IV_LENGTH_BYTES)
        secureRandom.nextBytes(iv)
        return iv
    }

    /**
     * Derive encryption key using PBKDF2.
     *
     * @param password Password/API key for key derivation
     * @param salt Salt for PBKDF2
     * @return Derived key bytes
     */
    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Securely wipe key material from memory.
     *
     * Call when CryptoManager is no longer needed.
     */
    fun wipeKeys() {
        encryptionKey.fill(0)
    }
}

/**
 * Security exception for cryptographic operation failures.
 */
internal class CryptoSecurityException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
