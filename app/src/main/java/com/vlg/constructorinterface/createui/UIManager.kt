package com.vlg.constructorinterface.createui

import android.content.ClipData
import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isEmpty
import com.vlg.constructorinterface.event.ActionExecutor
import com.vlg.constructorinterface.event.ElementAction
import com.vlg.constructorinterface.event.ElementEvent
import com.vlg.constructorinterface.filemanager.LayoutFileManager
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.event.toElementActionList
import com.vlg.constructorinterface.event.toJsonArray
import org.json.JSONArray
import java.io.File

class UIManager(private val context: Context, private val creatorUI: CreatorUI) {

    private val elementsMap = mutableMapOf<String, View>()
    private var layoutFileManager: LayoutFileManager = LayoutFileManager(context)
    private val listOfEditTexts: MutableList<EditText> = mutableListOf()

    fun getLayoutFileManager() = layoutFileManager
    fun getListOfEditTexts() = listOfEditTexts

    fun createElementFromData(
        elementData: UiElement,
        trashArea: LinearLayout? = null,
        placementHint: TextView? = null
    ): View {
        return createComponentFromData(elementData).apply {
            setOnLongClickListener { view ->
                val type = when (view) {
                    is TextView -> if (view !is Button && view !is EditText) "TEXTVIEW" else "UNKNOWN"
                    is EditText -> "EDITTEXT"
                    is Button -> "BUTTON"
                    else -> "UNKNOWN"
                }

                val item = ClipData.Item(type)
                val mimeTypes = arrayOf("text/plain")
                val data = ClipData(type, mimeTypes, item)

                val shadowBuilder = View.DragShadowBuilder(view)
                view.startDragAndDrop(data, shadowBuilder, view, 0)

                placementHint?.visibility = View.VISIBLE
                placementHint?.text = "Перетащите элемент. Отпустите для размещения в строке"
                trashArea?.visibility = View.VISIBLE

                true
            }

            setOnClickListener {
                Log.d("!!!122", this.tag.toString())
                creatorUI.handleDoubleClick(this)
            }
        }
    }

    fun createElementFromDataWithActions(
        elementData: UiElement,
        event: ElementEvent?,
        executor: ActionExecutor
    ): View {
        return createComponentFromData(elementData).apply {
            setOnClickListener {
                if (event != null) {
                    Log.d("!!! ui manager", event.toString())
                    executor.execute(event)
                } else {
                    Log.d("!!! ui manager", "event NULL")
                }
            }
        }
    }

    private fun createComponentFromData(elementData: UiElement) =
        when (elementData.type) {
            "TEXTVIEW" -> {
                TextView(context).apply {
                    text = elementData.text ?: "Текст"
                    textSize = 18f
                    setPadding(
                        creatorUI.dpToPx(16),
                        creatorUI.dpToPx(8),
                        creatorUI.dpToPx(16),
                        creatorUI.dpToPx(8)
                    )
                    setBackgroundResource(R.drawable.element_background)
                    tag = elementData.id
                    gravity = Gravity.CENTER
                    isClickable = true
                    elementsMap[elementData.id] = this
                }
            }

            "EDITTEXT" -> {
                val editText  = EditText(context).apply {
                    hint = elementData.hint ?: "Введите текст"
                    textSize = 16f
                    setPadding(
                        creatorUI.dpToPx(16),
                        creatorUI.dpToPx(8),
                        creatorUI.dpToPx(16),
                        creatorUI.dpToPx(8)
                    )
                    setBackgroundResource(R.drawable.element_background)
                    tag = elementData.id
                    gravity = Gravity.CENTER_VERTICAL
                    isClickable = true
                    elementsMap[elementData.id] = this

                    elementData.text?.let {
                        setText(it)
                    }
                }
                listOfEditTexts.add(editText)
                editText
            }

            "BUTTON" -> {
                Button(context).apply {
                    text = elementData.text ?: "Кнопка"
                    textSize = 16f
                    setPadding(
                        creatorUI.dpToPx(16),
                        creatorUI.dpToPx(8),
                        creatorUI.dpToPx(16),
                        creatorUI.dpToPx(8)
                    )
                    setBackgroundResource(R.drawable.button_background)
                    tag = elementData.id
                    isClickable = true
                    elementsMap[elementData.id] = this
                }
            }

            else -> creatorUI.createTextView()
        }

    fun saveCurrentLayout(workArea: LinearLayout): String {
        val rows = mutableListOf<RowData>()

        Log.d("SaveDebug", "Количество строк в workArea: ${workArea.childCount}")

        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            Log.d("SaveDebug", "Строка $i: класс=${child.javaClass.simpleName}")

            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL) {
                val elements = mutableListOf<UiElement>()

                Log.d("SaveDebug", "  Количество элементов в строке: ${child.childCount}")

                for (j in 0 until child.childCount) {
                    val element = child.getChildAt(j)
                    val elementId = element.tag?.toString()

                    if (elementId == null) {
                        Log.d(
                            "SaveDebug",
                            "  Элемент $j: НЕТ ТЕГА! класс=${element.javaClass.simpleName}"
                        )
                        continue
                    }

                    Log.d(
                        "SaveDebug",
                        "  Элемент $j: класс=${element.javaClass.simpleName}, tag=$elementId"
                    )

                    val uiElement = when (element) {
                        is EditText -> {
                            Log.d(
                                "SaveDebug",
                                "    Это EditText: hint=${element.hint}, text=${element.text}"
                            )
                            UiElement(
                                id = elementId,
                                type = "EDITTEXT",
                                hint = element.hint?.toString() ?: "",
                                text = element.text?.toString() ?: "",
                                position = Position(
                                    row = i,
                                    column = j,
                                    weight = getElementWeight(element),
                                    rowIndex = i
                                ),
                                size = Size(
                                    width = creatorUI.dpToPx(300),
                                    height = creatorUI.dpToPx(100)
                                )
                            )
                        }

                        is Button -> {
                            Log.d("SaveDebug", "    Это Button: text=${element.text}")
                            UiElement(
                                id = elementId,
                                type = "BUTTON",
                                text = element.text.toString(),
                                position = Position(
                                    row = i,
                                    column = j,
                                    weight = getElementWeight(element),
                                    rowIndex = i
                                ),
                                size = Size(
                                    width = creatorUI.dpToPx(200),
                                    height = creatorUI.dpToPx(100)
                                )
                            )
                        }

                        is TextView -> {
                            run {
                                Log.d("SaveDebug", "    Это TextView: text=${element.text}")
                                UiElement(
                                    id = elementId,
                                    type = "TEXTVIEW",
                                    text = element.text.toString(),
                                    position = Position(
                                        row = i,
                                        column = j,
                                        weight = getElementWeight(element),
                                        rowIndex = i
                                    ),
                                    size = Size(
                                        width = creatorUI.dpToPx(200),
                                        height = creatorUI.dpToPx(100)
                                    )
                                )
                            }
                        }

                        else -> null
                    }

                    uiElement?.let {
                        Log.d("!!!122 save", it.id)
                        elements.add(it)
                        Log.d("SaveDebug", "    Добавлен элемент типа ${it.type}")
                    }
                }

                rows.add(RowData(elements))
                Log.d("SaveDebug", "  Строка $i сохранена: ${elements.size} элементов")
            } else {
                Log.d("SaveDebug", "  Строка $i не является горизонтальным LinearLayout")
            }
        }

        val layout = UiLayout(
            rows = rows,
            screenWidth = context.resources.displayMetrics.widthPixels,
            screenHeight = context.resources.displayMetrics.heightPixels
        )

        val json = LayoutSerializer.saveLayout(layout)
        Log.d(
            "SaveDebug",
            "Всего строк: ${rows.size}, элементов: ${rows.sumOf { it.elements.size }}"
        )
        Log.d("SaveDebug", "JSON: $json")

        return json
    }

    fun saveLayoutToFile(workArea: LinearLayout, elementCounter: Int): Boolean {
        val json = saveCurrentLayout(workArea)
        val jsonActions = creatorUI.getActions().map { it.value }.toJsonArray()
        val success1 = layoutFileManager.saveLayoutToFile(json)
        val success2 = layoutFileManager.saveCounterToFile(elementCounter)
        val success3 = layoutFileManager.saveActionsToFile(jsonActions.toString())
        return success1 && success2 && success3
    }

    fun loadLayoutFromFile(): Pair<UiLayout?, Int> {
        val json = layoutFileManager.loadLayoutFromFile()
        val counter = layoutFileManager.loadCounterFromFile()

        return if (json != null) {
            try {
                val layout = LayoutSerializer.loadLayout(json)
                Pair(layout, counter)
            } catch (e: Exception) {
                Log.e("UIManager", "Error parsing layout JSON", e)
                Pair(null, counter)
            }
        } else {
            Pair(null, counter)
        }
    }

    fun restoreLayout(
        layout: UiLayout,
        workArea: LinearLayout,
        placementHint: TextView? = null,
        trashArea: LinearLayout? = null,
        executor: ActionExecutor? = null,
        actions: MutableMap<String, ElementAction>? = null
    ) {
        workArea.removeAllViews()
        elementsMap.clear()

        for (rowData in layout.rows) {
            val newRow = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, creatorUI.dpToPx(4), 0, creatorUI.dpToPx(4))
                }
                orientation = LinearLayout.HORIZONTAL
                setPadding(
                    creatorUI.dpToPx(8),
                    creatorUI.dpToPx(8),
                    creatorUI.dpToPx(8),
                    creatorUI.dpToPx(8)
                )
            }

            for (elementData in rowData.elements) {
                val element = if (executor == null)
                    createElementFromData(elementData, trashArea, placementHint)
                else {
                    Log.d("!!! restore ui manager", actions.toString())
                    createElementFromDataWithActions(
                        elementData,
                        actions?.get(elementData.id)?.event,
                        executor
                    )
                }

                val params = if (elementData.position.weight > 0) {
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        elementData.position.weight
                    ).apply {
                        setMargins(creatorUI.dpToPx(4), 0, creatorUI.dpToPx(4), 0)
                    }
                } else {
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                element.layoutParams = params
                newRow.addView(element)
            }

            workArea.addView(newRow)
        }

        if (workArea.isEmpty()) {
            creatorUI.addHintView(workArea)
        }
    }

    fun hasSavedLayout(): Boolean {
        return layoutFileManager.hasSavedLayout()
    }

    fun getLayoutFileInfo(): LayoutFileManager.FileInfo {
        return layoutFileManager.getLayoutFileInfo()
    }

    fun deleteLayoutFiles(): Boolean {
        elementsMap.clear()
        return layoutFileManager.deleteLayoutFile()
    }

    fun createBackup(): String {
        return layoutFileManager.createBackup()
    }

    fun getBackupFiles(): List<LayoutFileManager.BackupFileInfo> {
        return layoutFileManager.getBackupFiles()
    }

    fun restoreFromBackup(backupPath: String): Boolean {
        return layoutFileManager.restoreFromBackup(backupPath)
    }

    fun deleteBackup(backupPath: String): Boolean {
        return layoutFileManager.deleteBackup(backupPath)
    }

    fun exportLayoutJson(): String {
        return layoutFileManager.exportLayoutJson()
    }

    fun importLayoutJson(json: String): Boolean {
        return layoutFileManager.importLayoutJson(json)
    }

    fun getFileContentPreview(maxLines: Int = 20): String {
        return layoutFileManager.getFileContentPreview(maxLines)
    }

    private fun getElementWeight(element: View): Float {
        val params = element.layoutParams as? LinearLayout.LayoutParams
        return params?.weight ?: 1f
    }
}