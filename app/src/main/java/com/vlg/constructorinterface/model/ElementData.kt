package com.vlg.constructorinterface.model

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.gson.Gson

data class UiElement(
    val id: String,
    val type: Type,
    val text: String = "",
    val hint: String = "",
    val position: Position,
    val size: Size
)

data class ElementInfo(
    val tag: String,
    val displayName: String,
    val elementType: String,
    val currentText: String,
    val view: View? = null
) {
    override fun toString(): String {
        return displayName
    }
}

enum class Type(val type: String) {
    TEXTVIEW("TEXTVIEW"),
    EDITTEXT("EDITTEXT"),
    BUTTON("BUTTON"),
    SPINNER("SPINNER")
}

data class Position(
    val row: Int,
    val column: Int,
    val weight: Float,
    val rowIndex: Int
)

data class Size(
    val width: Int,
    val height: Int
)

data class RowData(
    val elements: List<UiElement>
)

data class UiLayout(
    val rows: List<RowData>,
    val screenWidth: Int,
    val screenHeight: Int
)

object LayoutSerializer {
    private val gson = Gson()

    fun saveLayout(layout: UiLayout): String {
        return gson.toJson(layout)
    }

    fun loadLayout(json: String): UiLayout {
        return gson.fromJson(json, UiLayout::class.java)
    }
}


fun View.extractText(): String? {
    return when (this) {
        is TextView -> text.toString()
        is EditText -> text.toString()
        is Button -> text.toString()
        is Spinner -> selectedItem.toString()
        else -> null
    }
}

fun View.setText(string: String) {
    when (this) {
        is TextView -> text = string
        is EditText -> setText(string)
        is Button -> text = string
        is Spinner -> {throw IllegalArgumentException("Spinner don`t allow insert text $string") }
    }
}

@SuppressLint("SetTextI18n")
fun View.addText(string: String) {
    when (this) {
        is TextView -> text = "${this.text} $string"
        is EditText -> setText("${this.text} $string")
        is Button -> text = "${this.text} $string"
        is Spinner -> {throw IllegalArgumentException("Spinner don`t allow insert text $string") }
    }
}
