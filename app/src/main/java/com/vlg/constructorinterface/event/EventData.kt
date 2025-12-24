package com.vlg.constructorinterface.event

import org.json.JSONArray
import org.json.JSONObject

sealed class ElementEvent {
    data class ShowToast(val message: String) : ElementEvent()
    data class ShowDialog(val title: String, val message: String) : ElementEvent()
    data class CreateEntry(val tableName: String) : ElementEvent()
    data class DeleteEntry(val tableName: String) : ElementEvent()
    data class OpenTable(val tableName: String): ElementEvent()
    data class GetTextFromEditText(val editTextId: Int) : ElementEvent()
    data class ChangeText(val newText: String) : ElementEvent()
    data class RunCustomCode(val code: String) : ElementEvent()
}

data class ElementAction(
    val event: ElementEvent,
    val targetId: String = "",
    val description: String = ""
)

fun List<ElementAction>.toJsonArray(): JSONArray {
    return JSONArray().apply {
        this@toJsonArray.forEach { action ->
            put(action.toJson())
        }
    }
}

fun ElementAction.toJson(): JSONObject {
    return JSONObject().apply {
        put("targetId", targetId)
        put("description", description)
        put("event", event.toJson())
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
            is ElementEvent.GetTextFromEditText -> {
                put("type", "GetTextFromEditText")
                put("editTextId", editTextId)
            }
            is ElementEvent.ChangeText -> {
                put("type", "ChangeText")
                put("newText", newText)
            }
            is ElementEvent.RunCustomCode -> {
                put("type", "RunCustomCode")
                put("code", code)
            }
            is ElementEvent.OpenTable -> {
                put("type", "")
                put("name", tableName)
            }
        }
    }
}

fun JSONArray.toElementActionList(): List<ElementAction> {
    val actions = mutableListOf<ElementAction>()
    for (i in 0 until this.length()) {
        val jsonObject = this.getJSONObject(i)
        actions.add(jsonObject.toElementAction())
    }
    return actions
}

fun JSONObject.toElementAction(): ElementAction {
    val targetId = this.optString("targetId", "")
    val description = this.optString("description", "")
    val event = this.getJSONObject("event").toElementEvent()
    return ElementAction(event, targetId, description)
}

fun JSONObject.toElementEvent(): ElementEvent {
    val type = this.getString("type")
    return when (type) {
        "ShowToast" -> {
            val message = this.getString("message")
            ElementEvent.ShowToast(message)
        }
        "ShowDialog" -> {
            val title = this.getString("title")
            val message = this.getString("message")
            ElementEvent.ShowDialog(title, message)
        }
        "CreateEntry" -> {
            val name = this.getString("name")
            ElementEvent.CreateEntry(name)
        }
        "DeleteEntry" -> {
            val name = this.getString("name")
            ElementEvent.DeleteEntry(name)
        }
        "GetTextFromEditText" -> {
            val editTextId = this.getInt("editTextId")
            ElementEvent.GetTextFromEditText(editTextId)
        }
        "ChangeText" -> {
            val newText = this.getString("newText")
            ElementEvent.ChangeText(newText)
        }
        "RunCustomCode" -> {
            val code = this.getString("code")
            ElementEvent.RunCustomCode(code)
        }
        "OpenTable" -> {
            val name = this.getString("name")
            ElementEvent.OpenTable(name)
        }
        else -> throw IllegalArgumentException("Unknown event type: $type")
    }
}
