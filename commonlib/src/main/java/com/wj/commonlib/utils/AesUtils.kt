package com.wj.commonlib.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 *
 * @author Admin
 * @version 1.0
 * @date 2018/6/12
 */
object AesUtils {
    // 加密
    @Throws(Exception::class)
    fun des_encrypt(sSrc: String, sKey: String?): String? {
        if (sKey == null) {
            print("Key为空null")
            return null
        }
        // 判断Key是否为16位
        if (sKey.length != 16) {
            print("Key长度不是16位")
            return null
        }
        val raw = sKey.toByteArray(charset("utf-8"))
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")//"算法/模式/补码方式"
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        val encrypted = cipher.doFinal(sSrc.toByteArray(charset("utf-8")))

        return Base64.encodeToString(encrypted,encrypted.size)//此处使用BASE64做转码功能，同时能起到2次加密的作用。
    }

    // 解密
    @Throws(Exception::class)
    fun des_decrypt(sSrc: String, sKey: String?): String? {
        try {
            // 判断Key是否正确
            if (sKey == null) {
                print("Key为空null")
                return null
            }
            // 判断Key是否为16位
            if (sKey.length != 16) {
                print("Key长度不是16位")
                return null
            }
            val raw = sKey.toByteArray(charset("utf-8"))
            val skeySpec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            val encrypted1 =Base64.decode(sSrc,Base64.DEFAULT)//先用base64解密
            return try {
                val original = cipher.doFinal(encrypted1)
               String(original)
            } catch (e: Exception) {
                println(e.toString())
                null
            }

        } catch (ex: Exception) {
            return null
        }
    }

    private val ALGORITHM = "AES/CBC/PKCS7Padding"

    /**
     * 加密
     * 256
     */
    fun AES_cbc_encrypt(srcData: String, key: String, iv: String): String {
        val keySpec = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv.toByteArray()))
        val encrypted = cipher.doFinal(srcData.toByteArray(charset("utf-8")))
        return  Base64.encodeToString(encrypted,encrypted.size)
    }

    //解密
    fun AES_cbc_decrypt(encData: String, key: String, iv: String): String {
        val keySpec = SecretKeySpec(key.toByteArray(), "AES")
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv.toByteArray()))
        val encData1 =Base64.decode(encData,Base64.DEFAULT)//先用base64解密
        return String(cipher.doFinal(encData1))
    }
}