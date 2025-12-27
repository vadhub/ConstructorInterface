package com.vlg.constructorinterface.createui.settingcomponent

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.vlg.constructorinterface.createui.CreatorUI
import com.vlg.constructorinterface.createui.customview.FakeSpinner
import com.vlg.constructorinterface.event.ElementAction

class ComponentTextUpdater(private val context: Context) {

    fun updateComponentText(view: View, newText: String) {
        if (newText.isNotEmpty()) {
            when (view) {
                is EditText -> view.hint = newText
                is TextView -> view.text = newText
                is Button -> view.text = newText
                is FakeSpinner -> view.text = newText
            }
            Toast.makeText(context, "Текст изменен", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateComponentID(view: View, newId: String, actions: MutableMap<String, ElementAction>) {

        val oldTag = view.tag

        if (actions.contains(oldTag)) {
            val value = actions[oldTag]
            actions.remove(oldTag)
            if (value != null)
                actions.put(newId, value)
        }

        view.tag = newId

        if (CreatorUI.getElementsMap().contains(oldTag)) {
            CreatorUI.getElementsMap().remove(oldTag)
            CreatorUI.getElementsMap().put(newId, view)
        }

        Toast.makeText(context, "Текст изменен", Toast.LENGTH_SHORT).show()
    }
}