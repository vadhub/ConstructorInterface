package com.vlg.constructorinterface.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.vlg.constructorinterface.Navigator
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.ui.createui.CreatorUI
import com.vlg.constructorinterface.ui.createui.DragManager
import com.vlg.constructorinterface.model.Type
import com.vlg.constructorinterface.ui.createui.UIManager
import com.vlg.constructorinterface.ui.createui.settingcomponent.SettingComponentDialog
import com.vlg.constructorinterface.domain.table.TableDataManager

class ConstructorFragment : Fragment() {
    private lateinit var workArea: LinearLayout
    private lateinit var placementHint: TextView
    private lateinit var trashArea: LinearLayout
    private lateinit var creatorUI: CreatorUI
    private lateinit var uiManager: UIManager
    private lateinit var mContext: Context
    private lateinit var navigator: Navigator
    private lateinit var tableDataManager: TableDataManager

    private val projectPath by lazy { arguments?.getString("PROJECT_PATH") }
    private val projectName by lazy { arguments?.getString("PROJECT_NAME") }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        navigator = context as Navigator
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.constructor_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        workArea = view.findViewById(R.id.workArea)
        placementHint = view.findViewById(R.id.placementHint)
        trashArea = view.findViewById(R.id.trashArea)
        tableDataManager = TableDataManager(mContext, projectPath ?: "")

        val textViewPalette = view.findViewById<LinearLayout>(R.id.textViewPalette)
        val editTextPalette = view.findViewById<LinearLayout>(R.id.editTextPalette)
        val buttonPalette = view.findViewById<LinearLayout>(R.id.buttonPalette)
        val spinnerPalette = view.findViewById<LinearLayout>(R.id.spinnerPalette)

        creatorUI = CreatorUI(
            view.context,
            layoutInflater,
            SettingComponentDialog(view.context, tableDataManager)
        )
        val dragManager = DragManager(workArea, requireActivity(), creatorUI)
        uiManager = UIManager(view.context, creatorUI, projectPath ?: "")

        textViewPalette.setOnLongClickListener { dragManager.startDrag(Type.TEXTVIEW.name, it, placementHint, trashArea) }
        editTextPalette.setOnLongClickListener { dragManager.startDrag(Type.EDITTEXT.name, it, placementHint, trashArea) }
        buttonPalette.setOnLongClickListener { dragManager.startDrag(Type.BUTTON.name, it, placementHint, trashArea) }
        spinnerPalette.setOnLongClickListener { dragManager.startDrag(Type.SPINNER.name, it, placementHint, trashArea) }

        workArea.setOnDragListener(dragManager.dragListener(placementHint, trashArea))
        trashArea.setOnDragListener(dragManager.trashDragListener(trashArea, placementHint))

        projectPath?.let {
            loadSavedLayout()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                saveCurrentLayout()
                Toast.makeText(requireContext(), "Интерфейс сохранен в файл", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_load -> {
                loadSavedLayout()
                true
            }
            R.id.menu_run -> {
                runProject()
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

    // ========== ФУНКЦИИ ДЛЯ СОХРАНЕНИЯ И ЗАГРУЗКИ ==========

    private fun runProject() {
        val fragment = RunFragment()
        val bundle = Bundle()
        bundle.putString("PROJECT_PATH", projectPath)
        bundle.putString("PROJECT_NAME", projectName)
        fragment.arguments = bundle
        navigator.startFragment(fragment)
    }


    private fun saveCurrentLayout() {
        val success = uiManager.saveLayoutToFile(workArea, creatorUI.getElementCounter())
        val success2 = tableDataManager.autoDetectSchemaAndSave(workArea)
        if (success && success2) {
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
                uiManager.restoreLayout(layout, workArea, placementHint, trashArea, isFakeLayout = true, actions = uiManager.getLayoutFileManager().loadActionsFromFile())
                Toast.makeText(mContext, "Интерфейс загружен из файла", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LayoutLoad", "Error restoring layout", e)
                Toast.makeText(mContext, "Ошибка загрузки интерфейса", Toast.LENGTH_SHORT).show()
                creatorUI.addHintView(workArea)
            }
        } else {
            creatorUI.addHintView(workArea)
        }
    }

    private fun clearLayout() {
        AlertDialog.Builder(mContext)
            .setTitle("Очистка интерфейса")
            .setMessage("Вы уверены, что хотите очистить весь интерфейс и удалить сохраненный файл?")
            .setPositiveButton("Очистить") { _, _ ->
                workArea.removeAllViews()
                creatorUI.setElementCounter(1)
                uiManager.deleteLayoutFiles()
                creatorUI.addHintView(workArea)
                Toast.makeText(mContext, "Интерфейс очищен, файл удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showFileInfo() {
        val fileInfo = uiManager.getLayoutFileInfo()

        AlertDialog.Builder(mContext)
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
                    Toast.makeText(mContext, "Файл удален", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun showExportDialog() {
        val json = uiManager.exportLayoutJson()

        AlertDialog.Builder(mContext)
            .setTitle("Экспорт интерфейса")
            .setMessage("Скопируйте JSON для экспорта:")
            .setView(EditText(mContext).apply {
                setText(json)
                setSelection(0, text.length)
                setPadding(creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8))
            })
            .setPositiveButton("Копировать") { _, _ ->
                val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("layout_json", json)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(mContext, "JSON скопирован в буфер обмена", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showImportDialog() {
        val editText = EditText(mContext)
        editText.hint = "Вставьте JSON здесь"
        editText.setPadding(creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8))

        AlertDialog.Builder(mContext)
            .setTitle("Импорт интерфейса")
            .setView(editText)
            .setPositiveButton("Импортировать") { _, _ ->
                val json = editText.text.toString()
                if (json.isNotEmpty()) {
                    val success = uiManager.importLayoutJson(json)
                    if (success) {
                        loadSavedLayout()
                        Toast.makeText(mContext, "Интерфейс импортирован из JSON", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(mContext, "Ошибка импорта: неверный формат JSON", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showBackupDialog() {
        val backupPath = uiManager.createBackup()
        if (backupPath.isNotEmpty()) {
            Toast.makeText(mContext, "Бэкап создан: $backupPath", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(mContext, "Ошибка создания бэкапа", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRestoreDialog() {
        val backups = uiManager.getBackupFiles()

        if (backups.isEmpty()) {
            Toast.makeText(mContext, "Нет доступных бэкапов", Toast.LENGTH_SHORT).show()
            return
        }

        val backupItems = backups.map { "${it.date} - ${it.sizeKB} KB - ${it.name}" }.toTypedArray()

        AlertDialog.Builder(mContext)
            .setTitle("Выберите бэкап для восстановления")
            .setItems(backupItems) { _, which ->
                val selectedBackup = backups[which]
                AlertDialog.Builder(mContext)
                    .setTitle("Восстановление")
                    .setMessage("Восстановить из бэкапа от ${selectedBackup.date}?")
                    .setPositiveButton("Восстановить") { _, _ ->
                        if (uiManager.restoreFromBackup(selectedBackup.path)) {
                            loadSavedLayout()
                            Toast.makeText(mContext, "Интерфейс восстановлен из бэкапа", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(mContext, "Ошибка восстановления", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Удалить") { _, _ ->
                        if (uiManager.deleteBackup(selectedBackup.path)) {
                            Toast.makeText(mContext, "Бэкап удален", Toast.LENGTH_SHORT).show()
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

        AlertDialog.Builder(mContext)
            .setTitle("Содержимое файла")
            .setMessage(content)
            .setPositiveButton("OK", null)
            .show()
    }

}