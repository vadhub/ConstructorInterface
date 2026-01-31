package com.vlg.constructorinterface.ui.createui

import com.vlg.constructorinterface.model.Element

class ElementManager {
    private var elementCounter = 0
    private val elementsMap = mutableMapOf<Int, Element>()

    fun getElementsMap() = elementsMap
    fun getElementCounter() = elementCounter
    fun setElementCounter(i: Int) { this.elementCounter = i }
    fun clearElementsMap() { elementsMap.clear() }

    fun addElement(elementId: Int, element: Element) {
        elementsMap[elementId] = element
    }

    fun removeElement(elementId: Int) {
        elementsMap.remove(elementId)
    }

    fun updateElement(elementId: Int, update: (Element) -> Unit) {
        elementsMap[elementId]?.let(update)
    }

    fun incrementCounter(): Int = elementCounter++
}