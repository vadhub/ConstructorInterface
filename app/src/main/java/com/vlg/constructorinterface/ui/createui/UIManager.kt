package com.vlg.constructorinterface.ui.createui

import android.content.ClipData
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isEmpty
import com.vlg.constructorinterface.ui.createui.customview.FakeSpinner
import com.vlg.constructorinterface.domain.event.ActionExecutor
import com.vlg.constructorinterface.model.ElementAction
import com.vlg.constructorinterface.model.ElementEvent
import com.vlg.constructorinterface.domain.filemanager.LayoutFileManager
import com.vlg.constructorinterface.model.LayoutSerializer
import com.vlg.constructorinterface.model.Position
import com.vlg.constructorinterface.model.RowData
import com.vlg.constructorinterface.model.Size
import com.vlg.constructorinterface.model.Element
import com.vlg.constructorinterface.model.Type
import com.vlg.constructorinterface.model.UiLayout
import com.vlg.constructorinterface.model.toJsonArray

class UIManager(
    private val context: Context,
    private val elementFactory: ElementFactory,
    private val elementManager: ElementManager,
    private val uiInteractionManager: UIInteractionManager,
    private val workAreaManager: WorkAreaManager,
    private val eventActionManager: EventActionManager,
    folderProject: String
) {

    private var layoutFileManager: LayoutFileManager = LayoutFileManager(context, folderProject)
    private val listOfEditTexts: MutableList<EditText> = mutableListOf()
    private val listOfSpinners: MutableList<Spinner> = mutableListOf()

    constructor(context: Context, creatorUI: CreatorUI, folderProject: String) : this(
        context = context,
        elementFactory = creatorUI.elementFactory,
        elementManager = creatorUI.elementManager,
        uiInteractionManager = creatorUI.uiInteractionManager,
        workAreaManager = creatorUI.workAreaManager,
        eventActionManager = creatorUI.eventActionManager,
        folderProject = folderProject
    )

    fun getLayoutFileManager() = layoutFileManager
    fun getListOfEditTexts() = listOfEditTexts
    fun getListOfSpinners() = listOfSpinners

    fun createElementFromData(
        workArea: LinearLayout,
        elementData: Element,
        trashArea: LinearLayout? = null,
        placementHint: TextView? = null,
        isFakeLayout: Boolean = false
    ): View {
        return createComponentFromData(elementData, isFakeLayout).apply {
            setOnLongClickListener { view ->
                val type = elementData.type

                val item = ClipData.Item(type.toString())
                val mimeTypes = arrayOf("text/plain")
                val data = ClipData(type.toString(), mimeTypes, item)

                val shadowBuilder = View.DragShadowBuilder(view)
                view.startDragAndDrop(data, shadowBuilder, view, 0)

                placementHint?.visibility = View.VISIBLE
                placementHint?.text = "Перетащите элемент. Отпустите для размещения в строке"
                trashArea?.visibility = View.VISIBLE

                true
            }

            if (this is Spinner) {
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        uiInteractionManager.handleDoubleClick(workArea, elementData)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            } else {
                setOnClickListener {
                    uiInteractionManager.handleDoubleClick(workArea, elementData)
                }
            }
        }
    }

    fun createElementFromDataWithActions(
        workArea: LinearLayout,
        elementData: Element,
        event: List<ElementEvent>?,
        executor: ActionExecutor,
        isFakeLayout: Boolean = false
    ): View {
        return createComponentFromData(elementData, isFakeLayout).apply {
            if (elementData.type == Type.SPINNER) {
                (this as Spinner).onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            uiInteractionManager.handleDoubleClick(workArea, elementData)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
            } else {
                setOnClickListener {
                    if (event != null) {
                        executor.execute(event)
                    } else {
                        Log.d("!!! ui manager", "event NULL" + " " + this.id + " " + elementData.type)
                    }
                }
            }
        }
    }

    private fun createComponentFromData(elementData: Element, isFakeLayout: Boolean = false) =
        when (elementData.type) {
            Type.TEXTVIEW -> {
                elementFactory.createTextView(element = elementData, elementId = elementData.id).first
            }

            Type.EDITTEXT -> {
                val editText = elementFactory.createEditText(element = elementData, elementId = elementData.id)
                listOfEditTexts.add(editText.first)
                editText.first
            }

            Type.BUTTON -> {
                elementFactory.createButton(element = elementData, elementId = elementData.id).first
            }

            Type.SPINNER -> {
                if (!isFakeLayout) {
                    val spinner = elementFactory.createSpinner(element = elementData, elementId = elementData.id)
                    listOfSpinners.add(spinner)
                    spinner
                } else {
                    elementFactory.createFakeSpinner(element = elementData, elementId = elementData.id).first
                }
            }
        }

    fun saveCurrentLayout(workArea: LinearLayout): String {
        val rows = mutableListOf<RowData>()

        Log.d("SaveDebug", "Количество строк в workArea: ${workArea.childCount}")

        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            Log.d("SaveDebug", "Строка $i: класс=${child.javaClass.simpleName}")

            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL) {
                val elements = mutableListOf<Element>()

                Log.d("SaveDebug", "  Количество элементов в строке: ${child.childCount}")

                for (j in 0 until child.childCount) {
                    val element = child.getChildAt(j)
                    val elementId = element.id

                    val uiElement = when (element) {
                        is EditText -> {
                            Element(
                                id = elementId,
                                tag = element.tag.toString(),
                                type = Type.EDITTEXT,
                                hint = element.hint?.toString() ?: "",
                                text = element.text?.toString() ?: "",
                                position = Position(
                                    row = i,
                                    column = j,
                                    weight = getElementWeight(element),
                                    rowIndex = i
                                ),
                                size = Size(
                                    width = UiUtils.dpToPx(context, 300),
                                    height = UiUtils.dpToPx(context, 100)
                                )
                            )
                        }

                        is Button -> {
                            Log.d("SaveDebug", "    Это Button: text=${element.text}")
                            Element(
                                id = elementId,
                                tag = element.tag.toString(),
                                type = Type.BUTTON,
                                text = element.text.toString(),
                                position = Position(
                                    row = i,
                                    column = j,
                                    weight = getElementWeight(element),
                                    rowIndex = i
                                ),
                                size = Size(
                                    width = UiUtils.dpToPx(context, 200),
                                    height = UiUtils.dpToPx(context, 100)
                                )
                            )
                        }

                        is FakeSpinner -> {
                            Log.d("SaveDebug", "    Это Spinner: text=${element.text}")
                            Element(
                                id = elementId,
                                tag = element.tag.toString(),
                                type = Type.SPINNER,
                                text = element.text.toString(),
                                position = Position(
                                    row = i,
                                    column = j,
                                    weight = getElementWeight(element),
                                    rowIndex = i
                                ),
                                size = Size(
                                    width = UiUtils.dpToPx(context, 200),
                                    height = UiUtils.dpToPx(context, 100)
                                )
                            )
                        }

                        is TextView -> {
                            Log.d("SaveDebug", "    Это TextView: text=${element.text}")
                            Element(
                                id = elementId,
                                tag = element.tag.toString(),
                                type = Type.TEXTVIEW,
                                text = element.text.toString(),
                                position = Position(
                                    row = i,
                                    column = j,
                                    weight = getElementWeight(element),
                                    rowIndex = i
                                ),
                                size = Size(
                                    width = UiUtils.dpToPx(context, 200),
                                    height = UiUtils.dpToPx(context, 100)
                                )
                            )
                        }

                        else -> null
                    }

                    uiElement?.let {
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
        Log.d("SaveDebug", "Всего строк: ${rows.size}, элементов: ${rows.sumOf { it.elements.size }}")
        Log.d("SaveDebug", "JSON: $json")

        return json
    }

    fun saveLayoutToFile(workArea: LinearLayout, elementCounter: Int): Boolean {
        val json = saveCurrentLayout(workArea)
        val jsonActions = eventActionManager.getActionsMap().map { it.value }.toJsonArray()
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
        actions: MutableMap<Int, ElementAction>? = null,
        isFakeLayout: Boolean = false
    ) {
        workArea.removeAllViews()
        elementManager.clearElementsMap()

        for (rowData in layout.rows) {
            val newRow = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, UiUtils.dpToPx(context, 4), 0, UiUtils.dpToPx(context, 4))
                }
                orientation = LinearLayout.HORIZONTAL
                setPadding(
                    UiUtils.dpToPx(context, 8),
                    UiUtils.dpToPx(context, 8),
                    UiUtils.dpToPx(context, 8),
                    UiUtils.dpToPx(context, 8)
                )
            }

            for (elementData in rowData.elements) {
                val element = if (executor == null)
                    createElementFromData(workArea, elementData, trashArea, placementHint, isFakeLayout)
                else {
                    Log.d("!!! restore ui manager", actions.toString())
                    createElementFromDataWithActions(workArea,
                        elementData,
                        actions?.get(elementData.id)?.events,
                        executor,
                        isFakeLayout
                    )
                }

                val params = if (elementData.position.weight > 0) {
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        elementData.position.weight
                    ).apply {
                        setMargins(UiUtils.dpToPx(context, 4), 0, UiUtils.dpToPx(context, 4), 0)
                    }
                } else {
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                element.layoutParams = params
                newRow.addView(element)

                // Добавляем элемент в менеджер
                elementManager.addElement(element.id, elementData)
            }

            workArea.addView(newRow)
        }

        if (workArea.isEmpty()) {
            workAreaManager.addHintView(workArea)
        }
    }

    fun getLayoutFileInfo(): LayoutFileManager.FileInfo {
        return layoutFileManager.getLayoutFileInfo()
    }

    fun deleteLayoutFiles(): Boolean {
        elementManager.clearElementsMap()
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