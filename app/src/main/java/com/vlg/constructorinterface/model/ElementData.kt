package com.vlg.constructorinterface.model

import com.google.gson.Gson

data class Element(
    val id: Int,
    var tag: String,
    val type: Type,
    var text: String = "",
    val hint: String = "",
    val position: Position,
    val size: Size
)

enum class Type() {
    TEXTVIEW,
    EDITTEXT,
    BUTTON,
    SPINNER
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
    val elements: List<Element>
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
