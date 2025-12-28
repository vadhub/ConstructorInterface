package com.vlg.constructorinterface.filemanager

import android.content.Context
import java.io.*
import java.nio.charset.Charset

object FileManager {
    fun saveToFile(
        context: Context,
        fileName: String,
        content: String,
        subDirectory: String? = null
    ) {
        val targetDir = if (subDirectory != null) {
            File(context.filesDir, subDirectory).apply { mkdirs() }
        } else {
            context.filesDir
        }

        val file = File(targetDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray(Charset.forName("UTF-8")))
        }
    }

    fun loadFromFile(
        context: Context,
        fileName: String,
        subDirectory: String? = null
    ): String? {
        val targetDir = if (subDirectory != null) {
            File(context.filesDir, subDirectory)
        } else {
            context.filesDir
        }

        val file = File(targetDir, fileName)
        if (!file.exists()) return null

        FileInputStream(file).use { inputStream ->
            val bytes = ByteArray(file.length().toInt())
            inputStream.read(bytes)
            return String(bytes, Charset.forName("UTF-8"))
        }
    }
}