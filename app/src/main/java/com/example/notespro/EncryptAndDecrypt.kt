package com.example.notespro

object EncryptAndDecrypt{

    private const val shift = 50

    fun encrypt(text: String): String {
        val encryptedText = StringBuilder()
        for (c in text) {
            encryptedText.append(String(Character.toChars(c.toInt() + shift)))
        }
        return encryptedText.toString()
    }


    fun decrypt(obfuscatedText: String): String {
        val decryptedText = StringBuilder()
        for (c in obfuscatedText) {
            decryptedText.append(String(Character.toChars(c.toInt() - shift)))
        }
        return decryptedText.toString()
    }

}