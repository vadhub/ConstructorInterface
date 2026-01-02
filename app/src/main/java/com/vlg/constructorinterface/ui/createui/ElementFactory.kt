package com.vlg.constructorinterface.ui.createui

import android.content.Context
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView

import com.vlg.constructorinterface.model.Element
import com.vlg.constructorinterface.model.Position
import com.vlg.constructorinterface.model.Size
import com.vlg.constructorinterface.model.Type
import com.vlg.constructorinterface.ui.createui.customview.FakeSpinner
import java.util.UUID

class ElementFactory(private val context: Context) {

    fun createAdapterSpinner(text: String): ArrayAdapter<String> {
        val listOf = text.split(",")
        return ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            listOf.toList()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    fun createTextView(element: Element? = null, elementId: Int): Pair<TextView, Element> {
        val textView = TextView(context).apply {
            id = element?.id ?: elementId
            text = element?.text ?: "Текст $elementId"
            textSize = 18f
            setPadding(
                UiUtils.dpToPx(context, 16), UiUtils.dpToPx(context, 8),
                UiUtils.dpToPx(context, 16), UiUtils.dpToPx(context, 8)
            )
            setBackgroundResource(com.vlg.constructorinterface.R.drawable.element_background)
            tag = element?.tag ?: UUID.randomUUID().toString()
            gravity = Gravity.CENTER
            isClickable = true
        }

        val newElement = createElementModel(element, textView, Type.TEXTVIEW, elementId)
        return Pair(textView, newElement)
    }

    fun createEditText(element: Element? = null, elementId: Int): Pair<EditText, Element> {
        val editText = EditText(context).apply {
            id = element?.id ?: elementId
            hint = element?.hint ?: "Введите текст $elementId"
            textSize = 16f
            setPadding(
                UiUtils.dpToPx(context, 16), UiUtils.dpToPx(context, 8),
                UiUtils.dpToPx(context, 16), UiUtils.dpToPx(context, 8)
            )
            setBackgroundResource(com.vlg.constructorinterface.R.drawable.element_background)
            tag = element?.tag ?: UUID.randomUUID().toString()
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
        }
        editText.setText(element?.text ?: "")

        val newElement = createElementModel(element, editText, Type.EDITTEXT, elementId)
        return Pair(editText, newElement)
    }

    fun createButton(element: Element? = null, elementId: Int): Pair<Button, Element> {
        val button = Button(context).apply {
            id = element?.id ?: elementId
            text = element?.text ?: "Кнопка $elementId"
            textSize = 16f
            setPadding(
                UiUtils.dpToPx(context, 16), UiUtils.dpToPx(context, 8),
                UiUtils.dpToPx(context, 16), UiUtils.dpToPx(context, 8)
            )
            setBackgroundResource(com.vlg.constructorinterface.R.drawable.button_background)
            tag = element?.id ?: UUID.randomUUID().toString()
            isClickable = true
        }

        val newElement = createElementModel(element, button, Type.BUTTON, elementId)
        return Pair(button, newElement)
    }

    fun createSpinner(element: Element? = null, elementId: Int): Spinner {
        val adapterList = createAdapterSpinner(element?.text ?: "")

        val spinner: Spinner = Spinner(context).apply {
            id = element?.id ?: elementId
            setPadding(
                UiUtils.dpToPx(context, 8),
                UiUtils.dpToPx(context, 8),
                UiUtils.dpToPx(context, 8),
                UiUtils.dpToPx(context, 8)
            )
            tag = element?.tag ?: UUID.randomUUID().toString()
            adapter = adapterList
        }

        return spinner
    }

    fun createFakeSpinner(element: Element? = null, elementId: Int): Pair<FakeSpinner, Element> {
        val spinnerFake = FakeSpinner(context).apply {
            id = element?.id ?: elementId
            text = element?.text ?: "Текст $elementId"
            textSize = 18f
            setBackgroundResource(com.vlg.constructorinterface.R.drawable.element_background)
            tag = element?.tag ?: UUID.randomUUID().toString()
            gravity = Gravity.CENTER
            isClickable = true
        }

        val newElement = createElementModel(element, spinnerFake, Type.SPINNER, elementId)
        return Pair(spinnerFake, newElement)
    }

    private fun createElementModel(
        element: Element?,
        view: TextView,
        type: Type,
        elementId: Int
    ): Element {
        return Element(
            id = element?.id ?: elementId,
            tag = view.tag.toString(),
            type = type,
            hint = view.hint?.toString() ?: "",
            text = view.text?.toString() ?: "",
            position = Position(
                row = element?.position?.row ?: 0,
                column = element?.position?.column ?: 0,
                weight = element?.position?.weight ?: 0.0f,
                rowIndex = element?.position?.rowIndex ?: 0
            ),
            size = Size(
                width = element?.size?.width ?: 0,
                height = element?.size?.height ?: 0
            )
        )
    }
}