package com.vlg.constructorinterface.ui.createui

import android.util.Log
import com.vlg.constructorinterface.model.ElementAction
import com.vlg.constructorinterface.model.ElementEvent

class EventActionManager {
    private val actions: MutableMap<Int, ElementAction> = mutableMapOf()

    fun getActionsMap() = actions
    fun setActions(actions: MutableMap<Int, ElementAction>) {
        this.actions.clear()
        this.actions.putAll(actions)
    }

    fun addOrUpdateElementAction(id: Int, action: ElementAction) {
        val existingAction = actions[id]

        if (existingAction != null) {
            existingAction.events.addAll(action.events)
            Log.d("EventActionManager", "Обновлено действие для элемента $id")
        } else {
            actions[id] = action
            Log.d("EventActionManager", "Добавлено действие для элемента $id")
        }
    }


    fun removeElementEvent(actionId: Int, eventToRemove: ElementEvent): Boolean {
        val action = actions[actionId] ?: run {
            Log.w("EventActionManager", "Действие с id=$actionId не найдено")
            return false
        }

        if (action.events.remove(eventToRemove)) {
            Log.d("EventActionManager", "Событие удалено из действия $actionId")
            return true
        } else {
            Log.w("EventActionManager", "Событие не найдено в действии $actionId")
            return false
        }
    }

    fun getEventsById(id: Int): List<ElementEvent> {
        return actions[id]?.events ?: emptyList()
    }

    fun getCountOfEventsById(id: Int): Int {
        return getEventsById(id).count()
    }
}