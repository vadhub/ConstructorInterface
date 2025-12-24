package com.vlg.constructorinterface

import com.google.gson.Gson
import com.vlg.constructorinterface.TableDataManager.TableSchema
import org.json.JSONArray
import org.json.JSONObject

sealed class ElementEvent {
    data class ShowToast(val message: String) : ElementEvent()
    data class ShowDialog(val title: String, val message: String) : ElementEvent()
    data class CreateEntry(val schema: TableSchema, val newRowId: Int, val values: Map<String, String>) : ElementEvent()
    data class DeleteEntry(val schema: TableSchema, val rowId: Int) : ElementEvent()
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
                put("schema", Gson().toJson(schema))
                put("newRowId", newRowId)
                put("values", JSONObject(values))
            }
            is ElementEvent.DeleteEntry -> {
                put("type", "DeleteEntry")
                put("schema", Gson().toJson(schema))
                put("rowId", rowId)
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
            val schemaJson = this.getString("schema")
            val schema = Gson().fromJson(schemaJson, TableSchema::class.java)
            val newRowId = this.getInt("newRowId")

            val valuesJson = this.getJSONObject("values")
            val values = mutableMapOf<String, String>()
            val keys = valuesJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                values[key] = valuesJson.getString(key)
            }

            ElementEvent.CreateEntry(schema, newRowId, values)
        }
        "DeleteEntry" -> {
            val schemaJson = this.getString("schema")
            val schema = Gson().fromJson(schemaJson, TableSchema::class.java)
            val rowId = this.getInt("rowId")
            ElementEvent.DeleteEntry(schema, rowId)
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
        else -> throw IllegalArgumentException("Unknown event type: $type")
    }
}
