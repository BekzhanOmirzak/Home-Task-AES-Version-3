package com.example.hometaskforaes

import android.media.MediaCodec.CryptoException
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKey

object AESEncryptionAndDecryption {

    private val TAG = javaClass.name.toString();
    private final val ALGO = "AES";
    private final val TRANS = "AES";

    @Throws(CryptoException::class)
    fun encrypt(key: SecretKey, inputFile: File, outputFile: File) {
        doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    @Throws(CryptoException::class)
    fun decrypt(key: SecretKey, inputFile: File, outputFile: File) {
        doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
    }

    fun doCrypto(cipherMode: Int, key: SecretKey, inputFile: File, outputFile: File) {
        try {

            val cipher = Cipher.getInstance(TRANS);
            cipher.init(cipherMode, key);

            val fileInputStream = FileInputStream(inputFile);
            val inputByte = ByteArray(inputFile.length().toInt());
            fileInputStream.read(inputByte);

            val outputByte = cipher.doFinal(inputByte);

            val fileOutputStream = FileOutputStream(outputFile);
            fileOutputStream.write(outputByte);

            fileInputStream.close();
            fileOutputStream.close();

        } catch (ex: Exception) {
            Log.e(TAG, "doCrypto: Exception occurred", ex);
        }
    }


}