package com.vlg.constructorinterface.ui.createui

import android.content.ClipData
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.vlg.constructorinterface.model.Element
import com.vlg.constructorinterface.model.Type
import com.vlg.constructorinterface.ui.createui.settingcomponent.SettingComponentFragment

class UIInteractionManager(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val elementManager: ElementManager,
    private val eventActionManager: EventActionManager,
    private val creatorUI: CreatorUI
) {
    private var lastClickTime: Long = 0
    private var lastClickedView: Element? = null

    fun createElement(
        workArea: LinearLayout,
        elementType: String,
        trashArea: LinearLayout,
        placementHint: TextView
    ): View {
        Log.d("DragDebug", "createElement: $elementType")

        val elementId = elementManager.incrementCounter()
        val elementFactory = ElementFactory(context)

        val elementPair = when (elementType) {
            Type.TEXTVIEW.name -> elementFactory.createTextView(elementId = elementId)
            Type.EDITTEXT.name -> elementFactory.createEditText(elementId = elementId)
            Type.BUTTON.name -> elementFactory.createButton(elementId = elementId)
            Type.SPINNER.name -> elementFactory.createFakeSpinner(elementId = elementId)
            else -> elementFactory.createTextView(elementId = elementId)
        }

        val view = elementPair.first
        val elementModel = elementPair.second

        elementManager.addElement(view.id, elementModel)
        setupElementListeners(view, elementModel, workArea, trashArea, placementHint)

        return view
    }


    private fun setupElementListeners(
        view: View,
        element: Element,
        workArea: LinearLayout,
        trashArea: LinearLayout,
        placementHint: TextView
    ) {
        view.setOnLongClickListener { v ->
            Log.d("DragDebug", "Long click on existing element")
            val type = when (v) {
                is EditText -> Type.EDITTEXT.name
                is Button -> Type.BUTTON.name
                is TextView -> Type.TEXTVIEW.name
                else -> "UNKNOWN"
            }

            val item = ClipData.Item(type)
            val mimeTypes = arrayOf("text/plain")
            val data = ClipData(type, mimeTypes, item)

            val shadowBuilder = View.DragShadowBuilder(v)
            v.startDragAndDrop(data, shadowBuilder, v, 0)

            placementHint.visibility = View.VISIBLE
            placementHint.text = "Перетащите элемент. Отпустите для размещения в строке"
            trashArea.visibility = View.VISIBLE
            true
        }

        view.setOnClickListener {
            handleDoubleClick(workArea, element)
        }
    }

    fun handleDoubleClick(workArea: LinearLayout, element: Element) {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastClickTime

        if (lastClickedView == element && timeDiff < 300) {
            showSettingComponentDialog(workArea, element).show(fragmentManager, "SettingComponent")
            lastClickTime = 0
            lastClickedView = null
        } else {
            lastClickTime = currentTime
            lastClickedView = element
        }
    }

    fun showSettingComponentDialog(workArea: LinearLayout, element: Element): DialogFragment {
        val settingFragment = SettingComponentFragment.Companion.newInstance(element.id, element.tag, element.text)
        settingFragment.setCreatorUI(creatorUI)
        settingFragment.setOnSettingCompleteListener(object :
            SettingComponentFragment.OnSettingCompleteListener {
            override fun onSettingsSaved(tag: String, newText: String, newTag: String) {
                elementManager.updateElement(element.id) {
                    it.tag = newTag
                    it.text = newText
                }

                if (element.type == Type.EDITTEXT) {
                    workArea.findViewById<EditText>(element.id).hint = newText
                } else {
                    workArea.findViewById<TextView>(element.id).text = newText
                }
            }

            override fun onSettingsCancelled() {}
        })

        settingFragment.setDeleteAction { i, event -> eventActionManager.removeElementEvent(i, event) }

        settingFragment.setUpdateInfoElement {
            Pair(eventActionManager.getEventsById(element.id),
                eventActionManager.getCountOfEventsById(element.id))
        }

        return settingFragment
    }
}