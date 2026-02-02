package com.vlg.constructorinterface.domain.event

import com.vlg.constructorinterface.model.ElementEvent


class ActionExecutor(private val eventDelegate: EventDelegate) {

    fun execute(event: List<ElementEvent>) {
        event.forEach {
            when (it) {
                is ElementEvent.ShowToast -> eventDelegate.eventToast(it)
                is ElementEvent.ShowDialog -> eventDelegate.eventDialog(it)
                is ElementEvent.CreateEntry -> eventDelegate.eventCreateEntry(it)
                is ElementEvent.DeleteEntry -> eventDelegate.eventDeleteEntry(it)
                is ElementEvent.OpenTable -> eventDelegate.eventOpenTable(it)
                is ElementEvent.AddText -> eventDelegate.eventAddText(it)
                is ElementEvent.ChangeText -> eventDelegate.eventChangeText(it)
                is ElementEvent.MathOperation -> eventDelegate.eventMath(it)
                else -> throw IllegalArgumentException("Unsupported event type: ${it::class.simpleName}")
            }
        }
    }
}
