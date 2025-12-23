package com.vlg.constructorinterface

import com.google.gson.Gson

data class UiElement(
    val id: String,
    val type: String, // TEXTVIEW, EDITTEXT, BUTTON
    val text: String? = null,
    val hint: String? = null,
    val position: Position,
    val size: Size,
    val properties: Map<String, Any> = emptyMap()
)

data class Position(
    val row: Int,
    val column: Int,
    val weight: Float = 1f,
    val rowIndex: Int
)

data class Size(
    val width: Int, // dp
    val height: Int // dp
)

data class RowData(
    val elements: List<UiElement>,
    val orientation: String = "HORIZONTAL"
)

data class UiLayout(
    val rows: List<RowData>,
    val screenWidth: Int,
    val screenHeight: Int,
    val timestamp: Long = System.currentTimeMillis()
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
