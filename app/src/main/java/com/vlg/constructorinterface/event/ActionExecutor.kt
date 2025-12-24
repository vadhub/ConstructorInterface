package com.vlg.constructorinterface.event


class ActionExecutor(private val eventDelegat: EventDelegat) {

    fun execute(event: ElementEvent) {
        when (event) {
            is ElementEvent.ShowToast -> eventDelegat.eventToast(event)
            is ElementEvent.ShowDialog -> eventDelegat.eventDialog(event)
            is ElementEvent.CreateEntry -> eventDelegat.eventCreateEntry(event)
            is ElementEvent.DeleteEntry -> eventDelegat.eventDeleteEntry(event)
            is ElementEvent.OpenTable -> eventDelegat.eventOpenTable(event)
            else -> ""
        }
    }
}