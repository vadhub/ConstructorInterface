package com.vlg.constructorinterface.filemanager

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

object FileManager {
    fun saveToFile(context: Context, fileName: String, content: String) {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray(Charset.forName("UTF-8")))
        }
    }

    fun loadFromFile(context: Context, fileName: String): String? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null

        FileInputStream(file).use { inputStream ->
            val bytes = ByteArray(file.length().toInt())
            inputStream.read(bytes)
            return String(bytes, Charset.forName("UTF-8"))
        }
    }
}