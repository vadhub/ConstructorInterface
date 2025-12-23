package com.vlg.constructorinterface

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColorInt

class DragManager(private val workArea: LinearLayout, private val activity: Activity, private val creatorUI: CreatorUI) {

    fun startDrag(elementType: String, view: View, placementHint: TextView, trashArea: LinearLayout): Boolean {
        Log.d("DragDebug", "startDrag: $elementType")

        val item = ClipData.Item(elementType)
        val mimeTypes = arrayOf("text/plain")
        val data = ClipData(elementType, mimeTypes, item)

        val shadowBuilder = View.DragShadowBuilder(view)
        view.startDragAndDrop(data, shadowBuilder, null, 0)
        view.alpha = 0.5f

        placementHint.visibility = View.VISIBLE
        placementHint.text = "Перетащите элемент. Отпустите для размещения в строке"
        trashArea.visibility = View.VISIBLE

        return true
    }

    fun dragListener(placementHint: TextView, trashArea: LinearLayout) = View.OnDragListener { _, event ->
        Log.d("DragDebug", "WorkArea drag action: ${event.action}, X=${event.x}, Y=${event.y}")

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d("DragDebug", "Drag started in work area")
                true
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d("DragDebug", "Drag entered work area")
                placementHint.text = "Выберите место размещения элемента"
                true
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d("DragDebug", "Drag exited work area")
                creatorUI.clearHighlights(workArea)
                placementHint.text = "Перетащите элемент на рабочую область"
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d("DragDebug", "Drag ended in work area")
                creatorUI.clearHighlights(workArea)
                placementHint.visibility = View.GONE
                trashArea.visibility = View.GONE

                activity.findViewById<LinearLayout>(R.id.textViewPalette).alpha = 1.0f
                activity.findViewById<LinearLayout>(R.id.editTextPalette).alpha = 1.0f
                activity.findViewById<LinearLayout>(R.id.buttonPalette).alpha = 1.0f
                true
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                val y = event.y
                creatorUI.findDropTarget(workArea, y, placementHint)
                true
            }

            DragEvent.ACTION_DROP -> {
                Log.d("DragDebug", "DROP in work area at X=${event.x}, Y=${event.y}")
                val item = event.clipData.getItemAt(0)
                val elementType = item.text.toString()
                Log.d("DragDebug", "Element type: $elementType")

                val draggedView = event.localState as? View

                if (draggedView != null) {
                    Log.d("DragDebug", "Moving existing element")
                    creatorUI.handleExistingElementMove(workArea,draggedView, event.x, event.y)
                } else {
                    Log.d("DragDebug", "Creating new element")
                    val newElement = creatorUI.createElement(elementType, trashArea, placementHint)
                    creatorUI.addElementToWorkArea(workArea, newElement, event.x, event.y)
                    Toast.makeText(activity, "Элемент добавлен!", Toast.LENGTH_SHORT).show()
                }

                creatorUI.clearHighlights(workArea)
                placementHint.visibility = View.GONE
                trashArea.visibility = View.GONE
                true
            }

            else -> {
                Log.d("DragDebug", "Unknown drag action in work area: ${event.action}")
                false
            }
        }
    }

    fun trashDragListener(trashArea: LinearLayout, placementHint: TextView) = View.OnDragListener { _, event ->
        Log.d("DragDebug", "Trash drag action: ${event.action}")

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                Log.d("DragDebug", "Drag started over trash")
                trashArea.setBackgroundColor("#FF8A80".toColorInt())
                true
            }

            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.d("DragDebug", "Drag entered trash area")
                trashArea.setBackgroundColor("#FF5252".toColorInt())
                placementHint.text = "Отпустите здесь для удаления элемента"
                true
            }

            DragEvent.ACTION_DRAG_EXITED -> {
                Log.d("DragDebug", "Drag exited trash area")
                trashArea.setBackgroundColor("#FFCDD2".toColorInt())
                placementHint.text = "Перетащите элемент на рабочую область"
                true
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                Log.d("DragDebug", "Drag ended over trash")
                trashArea.setBackgroundColor("#FFCDD2".toColorInt())
                trashArea.visibility = View.GONE
                placementHint.visibility = View.GONE
                true
            }

            DragEvent.ACTION_DROP -> {
                Log.d("DragDebug", "DROP in trash area")
                val draggedView = event.localState as? View

                if (draggedView != null) {
                    Log.d("DragDebug", "Dragged view found, showing confirmation dialog")
                    showDeleteConfirmationDialog(draggedView)
                } else {
                    Log.d("DragDebug", "No dragged view (new element from palette)")
                    trashArea.visibility = View.GONE
                    placementHint.visibility = View.GONE
                }

                trashArea.setBackgroundColor("#FFCDD2".toColorInt())
                true
            }

            else -> {
                Log.d("DragDebug", "Unknown drag action in trash: ${event.action}")
                false
            }
        }
    }

    private fun showDeleteConfirmationDialog(element: View) {
        val elementName = when (element) {
            is TextView -> if (element !is Button && element !is EditText)
                "Текстовое поле \"${element.text}\""
            else "Элемент"
            is EditText -> "Поле ввода"
            is Button -> "Кнопка \"${element.text}\""
            else -> "Элемент"
        }

        AlertDialog.Builder(activity)
            .setTitle("Удаление элемента")
            .setMessage("Вы уверены, что хотите удалить $elementName?")
            .setPositiveButton("Удалить") { _, _ ->
                creatorUI.deleteElementWithAnimation(workArea, element)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


}