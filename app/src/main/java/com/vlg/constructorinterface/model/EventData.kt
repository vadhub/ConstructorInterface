package com.vlg.constructorinterface.model

import org.json.JSONArray
import org.json.JSONObject

sealed class ElementEvent {
    data class ShowToast(val message: String) : ElementEvent()
    data class ShowDialog(val title: String, val message: String) : ElementEvent()
    data class CreateEntry(val tableName: String) : ElementEvent()
    data class DeleteEntry(val tableName: String) : ElementEvent()
    data class OpenTable(val tableName: String) : ElementEvent()
    data class GetTextFromEditText(val editTextId: Int) : ElementEvent()
    data class ChangeText(val newText: String, val idResult: Int) : ElementEvent()
    data class RunCustomCode(val code: String,) : ElementEvent()
    data class MathOperation(
        val expression: String,
        val resultVar: String? = null,
        val idResult: Int? = null
    ) : ElementEvent()
    data class AddText(
        val newText: String,
        val resultVar: String? = null,
        val idResult: Int? = null
    ) : ElementEvent()
    data class Empty(val message: String = "Empty")
}

fun ElementEvent.info(): String {
    return when(this) {
        is ElementEvent.AddText -> "Add Text"
        is ElementEvent.ChangeText -> "Change Text"
        is ElementEvent.CreateEntry -> "Create Entry"
        is ElementEvent.DeleteEntry -> "Delete Entry"
        is ElementEvent.GetTextFromEditText -> "Get Text From Edit Text"
        is ElementEvent.OpenTable -> "Open Table"
        is ElementEvent.MathOperation -> "Math Operation"
        is ElementEvent.RunCustomCode -> "Run Custom Code"
        is ElementEvent.ShowDialog -> "Show Dialog"
        is ElementEvent.ShowToast -> "Show Toast"
    }
}

fun ElementEvent.infoFull(): String {
    return when(this) {
        is ElementEvent.AddText -> "Add Text: newText: $newText, resultVar: $resultVar, idResult: $idResult"
        is ElementEvent.ChangeText -> "Change Text newText: $newText"
        is ElementEvent.CreateEntry -> "Create Entry tableName: $tableName"
        is ElementEvent.DeleteEntry -> "Delete Entry tableName: $tableName"
        is ElementEvent.GetTextFromEditText -> "Get Text From Edit Text editTextId: $editTextId"
        is ElementEvent.OpenTable -> "Open Table tableName: $tableName"
        is ElementEvent.MathOperation -> "Math Operation idResult: $idResult, resultVar: $resultVar, expression: $expression"
        is ElementEvent.RunCustomCode -> "Run Custom Code $code"
        is ElementEvent.ShowDialog -> "Show Dialog title $title, message: $message"
        is ElementEvent.ShowToast -> "Show Toast message $message"
    }
}

data class ElementAction(
    val id: Int,
    val events: MutableList<ElementEvent>,
    val targetId: Int = -1,
    val description: String = ""
)

enum class ActionType(val position: Int) {
    NONE(0),
    TOAST(1),
    DIALOG(2),
    CREATE_ENTRY(3),
    OPEN_TABLE(4),
    CHANGE_TEXT(5),
    ADD_TEXT(6),
    MATH_OPERATION(7);

    companion object {
        fun fromPosition(position: Int): ActionType? {
            return ActionType.entries.find { it.position == position }
        }
    }
}

fun List<ElementAction>.toJsonArray(): JSONArray {
    return JSONArray().apply {
        forEach { action -> put(action.toJson()) }
    }
}

fun ElementAction.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("targetId", targetId)
        put("description", description)
        put("events", JSONArray().apply {
            events.forEach { event ->
                put(event.toJson())
            }
        })
    }
}

fun ElementEvent.toJson(): JSONObject {
    return JSONObject().apply {
        when (this@toJson) {
            is ElementEvent.ShowToast -> {
                put("type", "ShowToast")
                put("message", message)
            }

            is ElementEvent.ShowDialog -> {
                put("type", "ShowDialog")
                put("title", title)
                put("message", message)
            }

            is ElementEvent.CreateEntry -> {
                put("type", "CreateEntry")
                put("tableName", tableName)
            }

            is ElementEvent.DeleteEntry -> {
                put("type", "DeleteEntry")
                put("tableName", tableName)
            }

            is ElementEvent.OpenTable -> {
                put("type", "OpenTable")
                put("tableName", tableName)
            }

            is ElementEvent.GetTextFromEditText -> {
                put("type", "GetTextFromEditText")
                put("editTextId", editTextId)
            }

            is ElementEvent.ChangeText -> {
                put("type", "ChangeText")
                put("newText", newText)
                put("idResult", idResult)
            }

            is ElementEvent.AddText -> {
                put("type", "AddText")
                put("newText", newText)
                put("resultVar", resultVar)
                put("idResult", idResult)
            }

            is ElementEvent.RunCustomCode -> {
                put("type", "RunCustomCode")
                put("code", code)
            }

            is ElementEvent.MathOperation -> {
                put("type", "MathOperation")
                put("expression", expression)
                put("resultVar", resultVar)
                put("idResult", idResult)
            }
        }
    }
}

fun JSONArray.toElementActionList(): List<ElementAction> {
    val actions = mutableListOf<ElementAction>()
    for (i in 0 until length()) {
        val jsonObject = getJSONObject(i)
        actions.add(jsonObject.toElementAction())
    }
    return actions
}

fun JSONObject.toElementAction(): ElementAction {
    val id = optInt("id", -1)
    val targetId = optInt("targetId", -1)
    val description = optString("description", "")

    val eventsJson = getJSONArray("events")
    val events = mutableListOf<ElementEvent>()
    for (i in 0 until eventsJson.length()) {
        val eventJson = eventsJson.getJSONObject(i)
        events.add(eventJson.toElementEvent())
    }

    return ElementAction(id, events, targetId, description)
}

fun JSONObject.toElementEvent(): ElementEvent {
    val type = getString("type")
    return when (type) {
        "ShowToast" -> {
            val message = getString("message")
            ElementEvent.ShowToast(message)
        }

        "ShowDialog" -> {
            val title = getString("title")
            val message = getString("message")
            ElementEvent.ShowDialog(title, message)
        }

        "CreateEntry" -> {
            val tableName = getString("tableName")
            ElementEvent.CreateEntry(tableName)
        }

        "DeleteEntry" -> {
            val tableName = getString("tableName")
            ElementEvent.DeleteEntry(tableName)
        }

        "OpenTable" -> {
            val tableName = getString("tableName")
            ElementEvent.OpenTable(tableName)
        }

        "GetTextFromEditText" -> {
            val editTextId = getInt("editTextId")
            ElementEvent.GetTextFromEditText(editTextId)
        }

        "ChangeText" -> {
            val newText = getString("newText")
            val idResult = optInt("idResult", -1)
            ElementEvent.ChangeText(newText, idResult)
        }

        "RunCustomCode" -> {
            val code = getString("code")
            ElementEvent.RunCustomCode(code)
        }

        "MathOperation" -> {
            val expression = getString("expression")
            val idResult = optInt("idResult", -1)
            val resultVar = optString("resultVar", "")
            ElementEvent.MathOperation(expression, resultVar, idResult)
        }

        "AddText" -> {
            val newText = getString("newText")
            val idResult = optInt("idResult", -1)
            val resultVar = optString("resultVar", "")
            ElementEvent.AddText(newText, resultVar, idResult)
        }

        else -> throw IllegalArgumentException("Unknown event type: $type")
    }
}
