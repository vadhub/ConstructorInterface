package com.vlg.constructorinterface.createui

import android.content.ClipData
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.view.isEmpty
import com.vlg.constructorinterface.event.ElementAction
import com.vlg.constructorinterface.R
import java.util.UUID

class CreatorUI(private val context: Context, private val layoutInflater: LayoutInflater, private val settingComponentDialog: SettingComponentDialog? = null) {

    private var lastClickTime: Long = 0
    private var lastClickedView: View? = null
    private var elementCounter = 1
    private var currentHighlightedRow: LinearLayout? = null
    private val elementsMap = mutableMapOf<String, View>()
    private val actions: MutableMap<String, ElementAction> = mutableMapOf() // tag -> EventAction

    companion object {
        fun createAdapterSpinner(context: Context, text: String): ArrayAdapter<String> {
            val listOf = text.split(",")
            val adapterList = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                listOf.toList()
            )
            adapterList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            return adapterList
        }
    }

    fun getActions() = actions
    fun getElementCounter() = elementCounter
    fun setElementCounter(i: Int) {
        this.elementCounter = i
    }
    fun clearElementsMap() {
        elementsMap.clear()
    }

    fun handleExistingElementMove(workArea: LinearLayout, element: View, x: Float, y: Float) {
        Log.d("DragDebug", "handleExistingElementMove")

        val parent = element.parent as? ViewGroup
        parent?.removeView(element)

        if (parent is LinearLayout && parent.orientation == LinearLayout.HORIZONTAL && parent.isEmpty()) {
            workArea.removeView(parent)
        }

        addElementToWorkArea(workArea, element, x, y)

        Toast.makeText(context, "Элемент перемещен!", Toast.LENGTH_SHORT).show()
    }

    fun createElement(elementType: String, trashArea: LinearLayout, placementHint: TextView): View {
        Log.d("DragDebug", "createElement: $elementType")

        return when (elementType) {
            "TEXTVIEW" -> createTextView()
            "EDITTEXT" -> createEditText()
            "BUTTON" -> createButton()
            else -> createTextView()
        }.apply {
            val elementId = UUID.randomUUID().toString()
            this.tag = elementId
            elementsMap[elementId] = this

            setOnLongClickListener { view ->
                Log.d("DragDebug", "Long click on existing element")
                val type = when (view) {
                    is EditText -> "EDITTEXT"
                    is Button -> "BUTTON"
                    is TextView -> "TEXTVIEW"
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
                handleDoubleClick(this)
            }
        }
    }

    fun handleDoubleClick(view: View) {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastClickTime

        if (lastClickedView == view && timeDiff < 300) {
            settingComponentDialog?.showDialog(layoutInflater, view, actions)
            lastClickTime = 0
            lastClickedView = null
        } else {
            when (view) {
                is TextView -> {
                    if (view !is Button && view !is EditText) {
                        Toast.makeText(context, "Текстовое поле", Toast.LENGTH_SHORT).show()
                    } else if (view is Button) {
                        Toast.makeText(context, "Кнопка", Toast.LENGTH_SHORT).show()
                    }
                }
                is Button -> {
                    Toast.makeText(context, "Кнопка", Toast.LENGTH_SHORT).show()
                }
            }

            lastClickTime = currentTime
            lastClickedView = view
        }
    }

    fun deleteElementWithAnimation(workArea: LinearLayout, element: View) {
        // Удаляем элемент из мапы
        val elementId = element.tag?.toString()
        if (elementId != null) {
            elementsMap.remove(elementId)
        }

        element.animate()
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(300)
            .withEndAction {
                val parent = element.parent as? ViewGroup
                parent?.removeView(element)

                if (parent is LinearLayout && parent.orientation == LinearLayout.HORIZONTAL && parent.isEmpty()) {
                    workArea.removeView(parent)
                }

                if (workArea.isEmpty()) {
                    addHintView(workArea)
                }

                Toast.makeText(context, "Элемент удален", Toast.LENGTH_SHORT).show()
            }
            .start()
    }

    fun addHintView(workArea: LinearLayout) {
        val hintView = TextView(context).apply {
            text = "Перетащите компоненты сюда"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            alpha = 0f
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        hintView.layoutParams = params
        workArea.addView(hintView)

        hintView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    fun clearHighlights(workArea: LinearLayout) {
        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL) {
                child.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        currentHighlightedRow = null
    }

    fun findDropTarget(workArea: LinearLayout, y: Float, placementHint: TextView) {
        clearHighlights(workArea)

        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL) {
                val top = child.top.toFloat()
                val bottom = child.bottom.toFloat()

                Log.d("DragDebug", "Row $i: top=$top, bottom=$bottom, y=$y")

                if (y in top..bottom) {
                    currentHighlightedRow = child
                    highlightRow(child, y - top, child.height.toFloat(), placementHint)
                    Log.d("DragDebug", "Found row at index $i")
                    return
                }
            }
        }

        placementHint.text = "Отпустите для создания новой строки"
        Log.d("DragDebug", "No row found, will create new one")
    }

    fun highlightRow(row: LinearLayout, relativeY: Float, rowHeight: Float, placementHint: TextView) {
        val halfHeight = rowHeight / 2

        if (relativeY < halfHeight) {
            row.setBackgroundColor("#E8F5E9".toColorInt())
            placementHint.text = "Верхняя половина: элемент займет всю строку"
            Log.d("DragDebug", "Top half of row")
        } else {
            if (row.childCount < 4) {
                row.setBackgroundColor("#E3F2FD".toColorInt())
                placementHint.text = "Нижняя половина: элемент добавится в строку (${row.childCount}/4)"
                Log.d("DragDebug", "Bottom half of row, can add (${row.childCount}/4)")
            } else {
                row.setBackgroundColor("#FFCDD2".toColorInt())
                placementHint.text = "Строка заполнена (4/4). Отпустите для создания новой строки ниже"
                Log.d("DragDebug", "Row is full (4/4)")
            }
        }
    }

    fun createTextView(element: UiElement? = null): TextView {
        val textView = TextView(context).apply {
            text = element?.text ?: "Текст $elementCounter"
            textSize = 18f
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            setBackgroundResource(R.drawable.element_background)
            tag = element?.id ?: UUID.randomUUID().toString() // Используем UUID вместо числа
            gravity = Gravity.CENTER
            isClickable = true
        }
        elementsMap[element?.id ?: textView.tag.toString()] = textView // Сохраняем в мапу
        elementCounter++
        return textView
    }

    fun createEditText(element: UiElement? = null): EditText {
        val editText = EditText(context).apply {
            hint = element?.hint ?: "Введите текст $elementCounter"
            textSize = 16f
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            setBackgroundResource(R.drawable.element_background)
            tag = element?.id ?: UUID.randomUUID().toString() // Используем UUID вместо числа
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
        }

        editText.setText(element?.text ?: "")
        elementsMap[element?.id ?:editText.tag.toString()] = editText // Сохраняем в мапу
        elementCounter++
        return editText
    }

    fun createButton(element: UiElement? = null): Button {
        val button = Button(context).apply {
            text = element?.text ?: "Кнопка $elementCounter"
            textSize = 16f
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            setBackgroundResource(R.drawable.button_background)
            tag = element?.id ?: UUID.randomUUID().toString()
            isClickable = true
        }
        elementsMap[element?.id ?:button.tag.toString()] = button
        elementCounter++
        return button
    }

    fun createSpinner(element: UiElement? = null): Spinner {

        val adapterList = createAdapterSpinner(context, element?.text ?: "")

        val spinner: Spinner = Spinner(context).apply {
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
            tag = element?.id ?: UUID.randomUUID().toString()
            adapter = adapterList
        }

        elementsMap[element?.id ?:spinner.tag.toString()] = spinner
        elementCounter++
        return spinner
    }

    fun addElementToWorkArea(workArea: LinearLayout, element: View, x: Float, y: Float) {
        Log.d("DragDebug", "addElementToWorkArea: x=$x, y=$y, workArea child count=${workArea.childCount}")

        if (workArea.childCount == 1 && workArea.getChildAt(0) is TextView) {
            val hintView = workArea.getChildAt(0) as TextView
            if (hintView.text == "Перетащите компоненты сюда") {
                workArea.removeAllViews()
                Log.d("DragDebug", "Removed hint view")
            }
        }

        var targetRow: LinearLayout? = null
        var rowIndex = -1

        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL) {
                val top = child.top.toFloat()
                val bottom = child.bottom.toFloat()

                Log.d("DragDebug", "Checking row $i: top=$top, bottom=$bottom, y=$y")

                if (y >= top && y <= bottom) {
                    targetRow = child
                    rowIndex = i
                    Log.d("DragDebug", "Found target row at index $i with ${child.childCount} children")
                    break
                }
            }
        }

        if (targetRow != null) {
            val rowHeight = targetRow.height.toFloat()
            val relativeY = y - targetRow.top

            Log.d("DragDebug", "Row height=$rowHeight, relativeY=$relativeY, half=${rowHeight/2}")

            if (relativeY < rowHeight / 2) {
                Log.d("DragDebug", "Top half - creating new row above")
                createNewRowAbove(workArea, element, rowIndex)
            } else {
                Log.d("DragDebug", "Bottom half - adding to existing row with ${targetRow.childCount} children")
                addToExistingRow(workArea, targetRow, element)
            }
        } else {
            Log.d("DragDebug", "No target row - creating new row")
            createNewRow(workArea, element, y)
        }

        Log.d("DragDebug", "After adding: workArea child count=${workArea.childCount}")
    }

    private fun createNewRowAbove(workArea: LinearLayout, element: View, rowIndex: Int) {
        Log.d("DragDebug", "createNewRowAbove: rowIndex=$rowIndex")

        val newRow = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(4), 0, dpToPx(4))
            }
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
        }

        element.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        newRow.addView(element)
        workArea.addView(newRow, rowIndex)
        Log.d("DragDebug", "Created new row above with 1 element")
    }

    private fun addToExistingRow(workArea: LinearLayout, row: LinearLayout, element: View) {
        Log.d("DragDebug", "addToExistingRow: row has ${row.childCount} children, max=4")

        if (row.childCount < 4) {
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ).apply {
                setMargins(dpToPx(4), 0, dpToPx(4), 0)
            }

            element.layoutParams = params
            row.addView(element)

            Log.d("DragDebug", "Added element to row, now has ${row.childCount} children")

            updateWeightsInRow(row)
        } else {
            Log.d("DragDebug", "Row full, creating new row below")
            val newRowIndex = workArea.indexOfChild(row) + 1
            val newRow = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, dpToPx(4), 0, dpToPx(4))
                }
                orientation = LinearLayout.HORIZONTAL
                setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            }

            element.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            newRow.addView(element)
            workArea.addView(newRow, newRowIndex)
        }
    }

    private fun updateWeightsInRow(row: LinearLayout) {
        Log.d("DragDebug", "updateWeightsInRow: row has ${row.childCount} children")

        for (i in 0 until row.childCount) {
            val child = row.getChildAt(i)
            val params = child.layoutParams as? LinearLayout.LayoutParams
            if (params != null) {
                params.weight = 1.0f
                params.width = 0
                params.setMargins(dpToPx(4), 0, dpToPx(4), 0)
                child.layoutParams = params
                Log.d("DragDebug", "Updated weight for child $i")
            }
        }

        row.requestLayout()
    }

    private fun createNewRow(workArea: LinearLayout, element: View, y: Float) {
        Log.d("DragDebug", "createNewRow at y=$y")

        val newRow = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dpToPx(4), 0, dpToPx(4))
            }
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
        }

        var insertPosition = workArea.childCount

        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            if (child is LinearLayout && child.orientation == LinearLayout.HORIZONTAL) {
                if (y < child.top) {
                    insertPosition = i
                    break
                }
            }
        }

        element.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        newRow.addView(element)
        workArea.addView(newRow, insertPosition)

        Log.d("DragDebug", "Created new row at position $insertPosition")
    }

    fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}