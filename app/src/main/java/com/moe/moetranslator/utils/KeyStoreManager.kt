/*
 * Copyright (C) 2024 murangogo
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.moe.moetranslator.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    fun storeKey(context: Context, secretKey: String, alias: String) {
        val prefs = CustomPreference.getInstance(context)
        // 创建一个 AES 密钥生成器，并指定使用 AndroidKeyStore 来生成密钥
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        // 指定密钥的属性
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keyGenParameterSpec)
        // 生成密钥并存储在 Android KeyStore 中
        val secretKeyFromKeystore = keyGenerator.generateKey()

        // 获取一个用于 AES-GCM 模式加密的 Cipher 实例
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // 初始化 Cipher 进行加密操作，使用从 KeyStore 中生成的密钥
        cipher.init(Cipher.ENCRYPT_MODE, secretKeyFromKeystore)
        // 将传入的明文密钥 secretKey 加密，返回的是加密后的字节数组
        val encryptedKey = cipher.doFinal(secretKey.toByteArray(Charsets.UTF_8))
        // 获取加密时使用的初始化向量 (IV)
        val iv = cipher.iv

        // 将加密后的密钥和 IV 编码为 Base64 字符串，方便存储
        val encryptedKeyBase64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
        val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)

        prefs.setString("${alias}_EncryptedKey", encryptedKeyBase64)
        prefs.setString("${alias}_IV", ivBase64)
    }

    fun retrieveKey(context: Context, alias: String): String? {
        // 从 KeyStore 中获取对应别名的密钥
        val secretKey = keyStore.getKey(alias, null) as? SecretKey ?: return null

        val prefs = CustomPreference.getInstance(context)
        val encryptedKeyBase64 = prefs.getString("${alias}_EncryptedKey", "")
        val ivBase64 = prefs.getString("${alias}_IV", "")

        if (encryptedKeyBase64 == "" || ivBase64 == "") {
            return null
        }

        val encryptedKey = Base64.decode(encryptedKeyBase64, Base64.DEFAULT)
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)

        // 获取用于 AES-GCM 模式解密的 Cipher 实例
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // 设置 GCM 模式的参数
        val spec = GCMParameterSpec(128, iv)
        // 初始化 Cipher 为解密模式，使用 Keystore 中存储的密钥和 GCM 参数
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        // 初始化 Cipher 为解密模式，使用 Keystore 中存储的密钥和 GCM 参数
        val decryptedBytes = cipher.doFinal(encryptedKey)
        // 将解密后的字节数组转换为字符串并返回
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun removeKey(context: Context, alias: String) {
        // 删除指定别名的密钥
        keyStore.deleteEntry(alias)
        val prefs = CustomPreference.getInstance(context)
        prefs.remove("${alias}_EncryptedKey")
        prefs.remove("${alias}_IV")
    }
}