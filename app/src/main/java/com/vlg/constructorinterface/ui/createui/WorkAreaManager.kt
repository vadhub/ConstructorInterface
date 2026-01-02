package com.vlg.constructorinterface.ui.createui

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isEmpty

class WorkAreaManager(
    private val context: Context,
    private val elementManager: ElementManager
) {
    fun deleteElementWithAnimation(workArea: LinearLayout, element: View) {
        val elementId = element.id
        elementManager.removeElement(elementId)

        element.animate()
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(300)
            .withEndAction {
                removeElementFromWorkArea(workArea, element)
                if (workArea.isEmpty()) {
                    addHintView(workArea)
                }
                Toast.makeText(context, "Элемент удален", Toast.LENGTH_SHORT).show()
            }
            .start()
    }

    private fun removeElementFromWorkArea(workArea: LinearLayout, element: View) {
        val parent = element.parent as? ViewGroup
        parent?.removeView(element)

        if (parent is LinearLayout && parent.orientation == LinearLayout.HORIZONTAL && parent.isEmpty()) {
            workArea.removeView(parent)
        }
    }

    fun addHintView(workArea: LinearLayout) {
        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            if (child is TextView && child.text == "Перетащите компоненты сюда") {
                return
            }
        }

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

    fun removeHintViewIfExists(workArea: LinearLayout) {
        for (i in 0 until workArea.childCount) {
            val child = workArea.getChildAt(i)
            if (child is TextView && child.text == "Перетащите компоненты сюда") {
                workArea.removeView(child)
                return
            }
        }
    }

    fun addElementToWorkArea(workArea: LinearLayout, element: View, x: Float, y: Float) {
        Log.d("DragDebug", "addElementToWorkArea: x=$x, y=$y, workArea child count=${workArea.childCount}")

        removeHintViewIfExists(workArea)

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

            Log.d("DragDebug", "Row height=$rowHeight, relativeY=$relativeY, half=${rowHeight / 2}")

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
        val newRow = createRowLayout()
        element.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        newRow.addView(element)
        workArea.addView(newRow, rowIndex)
    }

    private fun addToExistingRow(workArea: LinearLayout, row: LinearLayout, element: View) {
        if (row.childCount < 4) {
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ).apply {
                setMargins(UiUtils.dpToPx(context, 4), 0, UiUtils.dpToPx(context, 4), 0)
            }
            element.layoutParams = params
            row.addView(element)
            updateWeightsInRow(row)
        } else {
            val newRowIndex = workArea.indexOfChild(row) + 1
            val newRow = createRowLayout()
            element.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            newRow.addView(element)
            workArea.addView(newRow, newRowIndex)
        }
    }

    private fun createNewRow(workArea: LinearLayout, element: View, y: Float) {
        val newRow = createRowLayout()
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
    }

    private fun createRowLayout(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, UiUtils.dpToPx(context, 4), 0, UiUtils.dpToPx(context, 4))
            }
            orientation = LinearLayout.HORIZONTAL
            setPadding(UiUtils.dpToPx(context, 8), UiUtils.dpToPx(context, 8),
                UiUtils.dpToPx(context, 8), UiUtils.dpToPx(context, 8))
        }
    }

    private fun updateWeightsInRow(row: LinearLayout) {
        for (i in 0 until row.childCount) {
            val child = row.getChildAt(i)
            val params = child.layoutParams as? LinearLayout.LayoutParams
            if (params != null) {
                params.weight = 1.0f
                params.width = 0
                params.setMargins(UiUtils.dpToPx(context, 4), 0, UiUtils.dpToPx(context, 4), 0)
                child.layoutParams = params
            }
        }
        row.requestLayout()
    }
}