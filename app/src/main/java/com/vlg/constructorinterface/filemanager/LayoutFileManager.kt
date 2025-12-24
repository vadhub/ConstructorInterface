package com.vlg.constructorinterface.filemanager

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LayoutFileManager(private val context: Context) {

    companion object {
        private const val LAYOUT_FILE_NAME = "saved_layout.json"
        private const val COUNTER_FILE_NAME = "element_counter.txt"
        private const val BACKUP_FOLDER = "backups"
    }

    fun saveLayoutToFile(json: String): Boolean {
        return try {
            saveToFile(LAYOUT_FILE_NAME, json)
            true
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error saving layout to file", e)
            false
        }
    }

    fun loadLayoutFromFile(): String? {
        return try {
            loadFromFile(LAYOUT_FILE_NAME)
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error loading layout from file", e)
            null
        }
    }

    fun saveCounterToFile(counter: Int): Boolean {
        return try {
            saveToFile(COUNTER_FILE_NAME, counter.toString())
            true
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error saving counter to file", e)
            false
        }
    }

    fun loadCounterFromFile(): Int {
        return try {
            val content = loadFromFile(COUNTER_FILE_NAME)
            content?.toIntOrNull() ?: 1
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error loading counter from file", e)
            1
        }
    }

    fun createBackup(): String {
        return try {
            val backupDir = File(context.filesDir, BACKUP_FOLDER)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "layout_backup_$timestamp.json"
            val backupFile = File(backupDir, backupFileName)

            val layoutJson = loadLayoutFromFile() ?: "{}"

            FileOutputStream(backupFile).use { outputStream ->
                outputStream.write(layoutJson.toByteArray(Charset.forName("UTF-8")))
            }

            backupFile.absolutePath
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error creating backup", e)
            ""
        }
    }

    fun getBackupFiles(): List<BackupFileInfo> {
        return try {
            val backupDir = File(context.filesDir, BACKUP_FOLDER)
            if (!backupDir.exists() || !backupDir.isDirectory) {
                return emptyList()
            }

            backupDir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".json") }
                ?.map { file ->
                    val size = file.length() / 1024.0
                    val date = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(file.lastModified()))
                    BackupFileInfo(
                        name = file.name,
                        path = file.absolutePath,
                        sizeKB = String.format("%.1f", size),
                        date = date,
                        timestamp = file.lastModified()
                    )
                }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error getting backup files", e)
            emptyList()
        }
    }

    fun restoreFromBackup(backupPath: String): Boolean {
        return try {
            val backupFile = File(backupPath)
            if (!backupFile.exists()) return false

            FileInputStream(backupFile).use { inputStream ->
                val bytes = ByteArray(backupFile.length().toInt())
                inputStream.read(bytes)
                val json = String(bytes, Charset.forName("UTF-8"))

                // Проверяем валидность JSON
                JSONObject(json)

                // Сохраняем как текущий файл
                saveToFile(LAYOUT_FILE_NAME, json)
            }
            true
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error restoring from backup", e)
            false
        }
    }

    fun deleteBackup(backupPath: String): Boolean {
        return try {
            val backupFile = File(backupPath)
            if (backupFile.exists()) {
                backupFile.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error deleting backup", e)
            false
        }
    }

    fun deleteLayoutFile(): Boolean {
        return try {
            deleteFile(LAYOUT_FILE_NAME)
            deleteFile(COUNTER_FILE_NAME)
            true
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Error deleting layout files", e)
            false
        }
    }

    fun hasSavedLayout(): Boolean {
        return try {
            val file = File(context.filesDir, LAYOUT_FILE_NAME)
            file.exists() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getLayoutFileInfo(): FileInfo {
        return try {
            val layoutFile = File(context.filesDir, LAYOUT_FILE_NAME)

            if (layoutFile.exists()) {
                val size = layoutFile.length() / 1024.0
                val date = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(layoutFile.lastModified()))
                FileInfo(
                    exists = true,
                    name = LAYOUT_FILE_NAME,
                    sizeKB = String.format("%.1f", size),
                    date = date,
                    path = layoutFile.absolutePath
                )
            } else {
                FileInfo(
                    exists = false,
                    name = LAYOUT_FILE_NAME,
                    sizeKB = "0",
                    date = "Не создан",
                    path = ""
                )
            }
        } catch (e: Exception) {
            FileInfo(
                exists = false,
                name = LAYOUT_FILE_NAME,
                sizeKB = "0",
                date = "Ошибка",
                path = ""
            )
        }
    }

    fun exportLayoutJson(): String {
        return loadLayoutFromFile() ?: "{}"
    }

    fun importLayoutJson(json: String): Boolean {
        return try {
            // Проверяем валидность JSON
            JSONObject(json)
            saveToFile(LAYOUT_FILE_NAME, json)
            true
        } catch (e: Exception) {
            Log.e("LayoutFileManager", "Invalid JSON format", e)
            false
        }
    }

    fun getFileContentPreview(maxLines: Int = 20): String {
        return try {
            val json = loadLayoutFromFile() ?: return "Файл пуст"
            val lines = json.split("\n")
            if (lines.size <= maxLines) {
                json
            } else {
                lines.take(maxLines).joinToString("\n") + "\n... (показано $maxLines из ${lines.size} строк)"
            }
        } catch (e: Exception) {
            "Ошибка чтения файла: ${e.message}"
        }
    }

    private fun saveToFile(fileName: String, content: String) {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray(Charset.forName("UTF-8")))
        }
    }

    private fun loadFromFile(fileName: String): String? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null

        FileInputStream(file).use { inputStream ->
            val bytes = ByteArray(file.length().toInt())
            inputStream.read(bytes)
            return String(bytes, Charset.forName("UTF-8"))
        }
    }

    private fun deleteFile(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    data class FileInfo(
        val exists: Boolean,
        val name: String,
        val sizeKB: String,
        val date: String,
        val path: String
    )

    data class BackupFileInfo(
        val name: String,
        val path: String,
        val sizeKB: String,
        val date: String,
        val timestamp: Long
    )
}