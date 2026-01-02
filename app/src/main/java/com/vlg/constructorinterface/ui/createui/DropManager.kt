package com.vlg.constructorinterface.ui.createui

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.core.view.isEmpty

class DropManager(
    private val context: Context,
    private val workAreaManager: WorkAreaManager
) {
    private var currentHighlightedRow: LinearLayout? = null

    fun handleExistingElementMove(workArea: LinearLayout, element: View, x: Float, y: Float) {
        Log.d("DragDebug", "handleExistingElementMove")
        val parent = element.parent as? ViewGroup
        parent?.removeView(element)

        if (parent is LinearLayout && parent.orientation == LinearLayout.HORIZONTAL && parent.isEmpty()) {
            workArea.removeView(parent)
        }

        workAreaManager.addElementToWorkArea(workArea, element, x, y)
        Toast.makeText(context, "Элемент перемещен!", Toast.LENGTH_SHORT).show()
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
}