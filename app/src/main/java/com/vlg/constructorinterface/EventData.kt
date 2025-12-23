package com.vlg.constructorinterface

sealed class ElementEvent {
    data class ShowToast(val message: String) : ElementEvent()
    data class ShowDialog(val title: String, val message: String) : ElementEvent()
    data class GetTextFromEditText(val editTextId: Int) : ElementEvent()
    data class ChangeText(val newText: String) : ElementEvent()
    data class RunCustomCode(val code: String) : ElementEvent()
}

data class ElementAction(
    val event: ElementEvent,
    val targetId: Int = 0,
    val description: String = ""
)