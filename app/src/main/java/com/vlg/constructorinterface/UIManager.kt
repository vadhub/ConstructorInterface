package com.vlg.constructorinterface

import android.content.ClipData
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isEmpty
import java.util.UUID

class UIManager(private val context: Context, private val creatorUI: CreatorUI) {

    private val elementsMap = mutableMapOf<String, View>()
    private var layoutFileManager: LayoutFileManager = LayoutFileManager(context)

    fun createElementFromData(elementData: UiElement, trashArea: LinearLayout, placementHint: TextView): View {
        return when (elementData.type) {
            "TEXTVIEW" -> {
                TextView(context).apply {
                    text = elementData.text ?: "Текст"
                    textSize = 18f
                    setPadding(creatorUI.dpToPx(16), creatorUI.dpToPx(8), creatorUI.dpToPx(16), creatorUI.dpToPx(8))
                    setBackgroundResource(R.drawable.element_background)
                    tag = elementData.id
                    gravity = android.view.Gravity.CENTER
                    isClickable = true
                    elementsMap[elementData.id] = this
                }
            }
            "EDITTEXT" -> {
                EditText(context).apply {
                    hint = elementData.hint ?: "Введите текст"
                    textSize = 16f
                    setPadding(creatorUI.dpToPx(16), creatorUI.dpToPx(8), creatorUI.dpToPx(16), creatorUI.dpToPx(8))
                    setBackgroundResource(R.drawable.element_background)
                    tag = elementData.id
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    isClickable = true
                    elementsMap[elementData.id] = this
                }
            }
            "BUTTON" -> {
                Button(context).apply {
                    text = elementData.text ?: "Кнопка"
                    textSize = 16f
                    setPadding(creatorUI.dpToPx(16), creatorUI.dpToPx(8), creatorUI.dpToPx(16), creatorUI.dpToPx(8))
                    setBackgroundResource(R.drawable.button_background)
                    tag = elementData.id
                    isClickable = true
                    elementsMap[elementData.id] = this
                }
            }
            else -> creatorUI.createTextView()
        }.apply {
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

                placementHint.visibility = View.VISIBLE
                placementHint.text = "Перетащите элемент. Отпустите для размещения в строке"
                trashArea.visibility = View.VISIBLE

                true
            }

            setOnClickListener {
                creatorUI.handleDoubleClick(this)
            }
        }
    }

    fun saveCurrentLayout(workArea: LinearLayout): String {
        val rows = mutableListOf<RowData>()

        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL) {
                val elements = mutableListOf<UiElement>()

                for (j in 0 until child.childCount) {
                    val element = child.getChildAt(j)
                    val elementId = element.tag?.toString() ?: UUID.randomUUID().toString()

                    val uiElement = when (element) {
                        is TextView -> {
                            if (element !is Button && element !is EditText) {
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
                            } else if (element is Button) {
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
                            } else {
                                null
                            }
                        }
                        is EditText -> {
                            UiElement(
                                id = elementId,
                                type = "EDITTEXT",
                                hint = element.hint.toString(),
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
                        else -> null
                    }

                    uiElement?.let { elements.add(it) }
                }

                rows.add(RowData(elements))
            }
        }

        val layout = UiLayout(
            rows = rows,
            screenWidth = context.resources.displayMetrics.widthPixels,
            screenHeight = context.resources.displayMetrics.heightPixels
        )

        val json = LayoutSerializer.saveLayout(layout)
        Log.d("LayoutSave", "Layout saved: ${rows.size} rows, ${rows.sumOf { it.elements.size }} elements")

        return json
    }

    fun saveLayoutToFile(workArea: LinearLayout, elementCounter: Int): Boolean {
        val json = saveCurrentLayout(workArea)
        val success1 = layoutFileManager.saveLayoutToFile(json)
        val success2 = layoutFileManager.saveCounterToFile(elementCounter)
        return success1 && success2
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

    fun restoreLayout(layout: UiLayout, workArea: LinearLayout, placementHint: TextView, trashArea: LinearLayout) {
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
                setPadding(creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8), creatorUI.dpToPx(8))
            }

            for (elementData in rowData.elements) {
                val element = createElementFromData(elementData, trashArea, placementHint)

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