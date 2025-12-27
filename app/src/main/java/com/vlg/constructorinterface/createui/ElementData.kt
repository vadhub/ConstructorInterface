package com.vlg.constructorinterface.createui

import android.view.View
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
