package com.vlg.constructorinterface.event


class ActionExecutor(private val eventDelegat: EventDelegat) {

    fun execute(event: List<ElementEvent>) {
        event.forEach {
            when (it) {
                is ElementEvent.ShowToast -> eventDelegat.eventToast(it)
                is ElementEvent.ShowDialog -> eventDelegat.eventDialog(it)
                is ElementEvent.CreateEntry -> eventDelegat.eventCreateEntry(it)
                is ElementEvent.DeleteEntry -> eventDelegat.eventDeleteEntry(it)
                is ElementEvent.OpenTable -> eventDelegat.eventOpenTable(it)
                else -> ""
            }
        }
    }
}