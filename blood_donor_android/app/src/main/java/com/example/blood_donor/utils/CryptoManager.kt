package com.example.blood_donor.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "secret_credentials_key"

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) return existingKey

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    fun encrypt(text: String): String {
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
            
            // Combine IV and Encrypted Bytes for storage
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            return ""
        }
    }

    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isBlank()) return ""
        try {
            val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val iv = ByteArray(12) // GCM standard IV size
            System.arraycopy(combined, 0, iv, 0, iv.size)
            
            val encryptedBytes = ByteArray(combined.size - iv.size)
            System.arraycopy(combined, iv.size, encryptedBytes, 0, encryptedBytes.size)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
            
            return String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            return ""
        }
    }
}
