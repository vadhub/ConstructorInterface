package com.vlg.constructorinterface

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.vlg.constructorinterface.createui.CreatorUI
import com.vlg.constructorinterface.createui.DragManager
import com.vlg.constructorinterface.createui.SettingComponentDialog
import com.vlg.constructorinterface.createui.UIManager

class MainActivity : AppCompatActivity(), Navigator {

    private lateinit var workArea: LinearLayout
    private lateinit var placementHint: TextView
    private lateinit var trashArea: LinearLayout
    private lateinit var creatorUI: CreatorUI
    private lateinit var uiManager: UIManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        workArea = findViewById(R.id.workArea)
        placementHint = findViewById(R.id.placementHint)
        trashArea = findViewById(R.id.trashArea)

        val textViewPalette = findViewById<LinearLayout>(R.id.textViewPalette)
        val editTextPalette = findViewById<LinearLayout>(R.id.editTextPalette)
        val buttonPalette = findViewById<LinearLayout>(R.id.buttonPalette)

        creatorUI = CreatorUI(this, layoutInflater, SettingComponentDialog(this))
        val dragManager = DragManager(workArea, this, creatorUI)
        uiManager = UIManager(this, creatorUI)

        textViewPalette.setOnLongClickListener { dragManager.startDrag("TEXTVIEW", it, placementHint, trashArea) }
        editTextPalette.setOnLongClickListener { dragManager.startDrag("EDITTEXT", it, placementHint, trashArea) }
        buttonPalette.setOnLongClickListener { dragManager.startDrag("BUTTON", it, placementHint, trashArea) }

        workArea.setOnDragListener(dragManager.dragListener(placementHint, trashArea))
        trashArea.setOnDragListener(dragManager.trashDragListener(trashArea, placementHint))
    }

    // ========== ФУНКЦИИ ДЛЯ СОХРАНЕНИЯ И ЗАГРУЗКИ ==========

    private fun saveCurrentLayout() {
        val success = uiManager.saveLayoutToFile(workArea, creatorUI.getElementCounter())
        if (success) {
            Log.d("LayoutSave", "Layout saved to file")
        } else {
            Log.e("LayoutSave", "Failed to save layout to file")
        }
    }

    private fun loadSavedLayout() {
        val (layout, counter) = uiManager.loadLayoutFromFile()
        creatorUI.setElementCounter(counter)

        if (layout != null) {
            try {
                uiManager.restoreLayout(layout, workArea, placementHint, trashArea)
                Toast.makeText(this, "Интерфейс загружен из файла", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LayoutLoad", "Error restoring layout", e)
                Toast.makeText(this, "Ошибка загрузки интерфейса", Toast.LENGTH_SHORT).show()
                creatorUI.addHintView(workArea)
            }
        } else {
            creatorUI.addHintView(workArea)
        }
    }

    // ========== МЕНЮ ДЛЯ СОХРАНЕНИЯ/ЗАГРУЗКИ ==========

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                saveCurrentLayout()
                Toast.makeText(this, "Интерфейс сохранен в файл", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_load -> {
                loadSavedLayout()
                true
            }
            R.id.menu_export -> {
                showExportDialog()
                true
            }
            R.id.menu_import -> {
                showImportDialog()
                true
            }
            R.id.menu_clear -> {
                clearLayout()
                true
            }
            R.id.menu_info -> {
                showFileInfo()
                true
            }
            R.id.menu_backup -> {
                showBackupDialog()
                true
            }
            R.id.menu_restore -> {
                showRestoreDialog()
                true
            }
            R.id.menu_view_content -> {
                showFileContent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearLayout() {
        AlertDialog.Builder(this)
            .setTitle("Очистка интерфейса")
            .setMessage("Вы уверены, что хотите очистить весь интерфейс и удалить сохраненный файл?")
            .setPositiveButton("Очистить") { _, _ ->
                workArea.removeAllViews()
                creatorUI.setElementCounter(1)
                uiManager.deleteLayoutFiles()
                creatorUI.addHintView(workArea)
                Toast.makeText(this, "Интерфейс очищен, файл удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showFileInfo() {
        val fileInfo = uiManager.getLayoutFileInfo()

        AlertDialog.Builder(this)
            .setTitle("Информация о файле")
            .setMessage(
                if (fileInfo.exists) {
                    "Файл макета:\n\n" +
                            "Имя: ${fileInfo.name}\n" +
                            "Размер: ${fileInfo.sizeKB} KB\n" +
                            "Дата: ${fileInfo.date}\n" +
                            "Путь: ${fileInfo.path}\n\n" +
                            "Количество элементов: ${workArea.childCount}"
                } else {
                    "Сохраненный макет не найден\n\n" +
                            "Создайте и сохраните интерфейс"
                }
            )
            .setPositiveButton("OK", null)
            .setNegativeButton("Удалить файл") { _, _ ->
                if (uiManager.deleteLayoutFiles()) {
                    Toast.makeText(this, "Файл удален", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showExportDialog() {
        val json = uiManager.exportLayoutJson()

        AlertDialog.Builder(this)
            .setTitle("Экспорт интерфейса")
            .setMessage("Скопируйте JSON для экспорта:")
            .setView(EditText(this).apply {
                setText(json)
                setSelection(0, text.length)
                setPadding(creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8))
            })
            .setPositiveButton("Копировать") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("layout_json", json)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "JSON скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showImportDialog() {
        val editText = EditText(this)
        editText.hint = "Вставьте JSON здесь"
        editText.setPadding(creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8))

        AlertDialog.Builder(this)
            .setTitle("Импорт интерфейса")
            .setView(editText)
            .setPositiveButton("Импортировать") { _, _ ->
                val json = editText.text.toString()
                if (json.isNotEmpty()) {
                    val success = uiManager.importLayoutJson(json)
                    if (success) {
                        loadSavedLayout()
                        Toast.makeText(this, "Интерфейс импортирован из JSON", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Ошибка импорта: неверный формат JSON", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showBackupDialog() {
        val backupPath = uiManager.createBackup()
        if (backupPath.isNotEmpty()) {
            Toast.makeText(this, "Бэкап создан: $backupPath", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Ошибка создания бэкапа", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRestoreDialog() {
        val backups = uiManager.getBackupFiles()

        if (backups.isEmpty()) {
            Toast.makeText(this, "Нет доступных бэкапов", Toast.LENGTH_SHORT).show()
            return
        }

        val backupItems = backups.map { "${it.date} - ${it.sizeKB} KB - ${it.name}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Выберите бэкап для восстановления")
            .setItems(backupItems) { _, which ->
                val selectedBackup = backups[which]
                AlertDialog.Builder(this)
                    .setTitle("Восстановление")
                    .setMessage("Восстановить из бэкапа от ${selectedBackup.date}?")
                    .setPositiveButton("Восстановить") { _, _ ->
                        if (uiManager.restoreFromBackup(selectedBackup.path)) {
                            loadSavedLayout()
                            Toast.makeText(this, "Интерфейс восстановлен из бэкапа", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Ошибка восстановления", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Удалить") { _, _ ->
                        if (uiManager.deleteBackup(selectedBackup.path)) {
                            Toast.makeText(this, "Бэкап удален", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNeutralButton("Отмена", null)
                    .show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showFileContent() {
        val content = uiManager.getFileContentPreview()

        AlertDialog.Builder(this)
            .setTitle("Содержимое файла")
            .setMessage(content)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun startFragment(fragment: Fragment) {
        // support
    }

}