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
    data class MathOperation(val terms: List<Term>, val operationType: OperationType) : ElementEvent() {
        fun isValid(): Boolean {
            return terms.isNotEmpty() &&
                    (operationType != OperationType.DIVISION || terms.all { it.data.toDouble() != 0.0 })
        }
    }
}

enum class OperationType {
    ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, POWER
}

data class ElementAction(
    val events: MutableList<ElementEvent>,
    val targetId: String = "",
    val description: String = ""
)

fun List<ElementAction>.toJsonArray(): JSONArray {
    return JSONArray().apply {
        forEach { action -> put(action.toJson()) }
    }
}

fun ElementAction.toJson(): JSONObject {
    return JSONObject().apply {
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
            }
            is ElementEvent.RunCustomCode -> {
                put("type", "RunCustomCode")
                put("code", code)
            }
            is ElementEvent.MathOperation -> {
                put("type", "MathOperation")
                put("terms", JSONArray().apply {
                    terms.forEach { term ->
                        put(JSONObject().apply {
                            put("isConst", term.isConst)
                            put("data", term.data)
                        })
                    }
                })
                put("operationType", operationType.name)
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
    val targetId = optString("targetId", "")
    val description = optString("description", "")

    val eventsJson = getJSONArray("events")
    val events = mutableListOf<ElementEvent>()
    for (i in 0 until eventsJson.length()) {
        val eventJson = eventsJson.getJSONObject(i)
        events.add(eventJson.toElementEvent())
    }

    return ElementAction(events, targetId, description)
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
            ElementEvent.ChangeText(newText)
        }
        "RunCustomCode" -> {
            val code = getString("code")
            ElementEvent.RunCustomCode(code)
        }
        "MathOperation" -> {
            val termsJson = getJSONArray("terms")
            val terms = mutableListOf<Term>()
            for (i in 0 until termsJson.length()) {
                val termJson = termsJson.getJSONObject(i)
                val isConst = termJson.getBoolean("isConst")
                val data = termJson.get("data")
                terms.add(Term(isConst, data as Number))
            }
            val operationType = OperationType.valueOf(getString("operationType"))
            ElementEvent.MathOperation(terms, operationType)
        }
        else -> throw IllegalArgumentException("Unknown event type: $type")
    }
}
