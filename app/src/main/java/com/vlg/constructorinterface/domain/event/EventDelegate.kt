package com.vlg.constructorinterface.domain.event

import com.vlg.constructorinterface.model.ElementEvent

class EventDelegate() {

    private var createEntry: (ElementEvent.CreateEntry) -> Unit = {}
    private var deleteEntry: (ElementEvent.DeleteEntry) -> Unit = {}
    private var openTable: (ElementEvent.OpenTable) -> Unit = {}
    private var mathOperation: (ElementEvent.MathOperation) -> Unit = {}
    private var addText: (ElementEvent.AddText) -> Unit = {}
    private var toastEvent: (ElementEvent.ShowToast) -> Unit = {}
    private var dialogEvent: (ElementEvent.ShowDialog) -> Unit = {}

    fun setOnCreateEntry(createEntry: (ElementEvent.CreateEntry) -> Unit) {
        this.createEntry = createEntry
    }

    fun setOnDeleteEntry(deleteEntry: (ElementEvent.DeleteEntry) -> Unit) {
        this.deleteEntry = deleteEntry
    }

    fun setOnOpenTable(openTable: (ElementEvent.OpenTable) -> Unit) {
        this.openTable = openTable
    }

    fun setOnMathOperation(mathOperation: (ElementEvent.MathOperation) -> Unit) {
        this.mathOperation = mathOperation
    }

    fun setOnAddText(addText: (ElementEvent.AddText) -> Unit) {
        this.addText = addText
    }

    fun setOnShowToast(showToast: (ElementEvent.ShowToast) -> Unit) {
        this.toastEvent = showToast
    }

    fun setOnShowDialog(showDialog: (ElementEvent.ShowDialog) -> Unit) {
        this.dialogEvent = showDialog
    }

    fun eventToast(event: ElementEvent.ShowToast) {
        toastEvent.invoke(event)
    }

    fun eventDialog(event: ElementEvent.ShowDialog) {
        dialogEvent.invoke(event)
    }

    fun eventCreateEntry(event: ElementEvent.CreateEntry) {
        createEntry.invoke(event)
    }

    fun eventDeleteEntry(event: ElementEvent.DeleteEntry) {
        deleteEntry.invoke(event)
    }

    fun eventOpenTable(event: ElementEvent.OpenTable) {
        openTable.invoke(event)
    }

    fun eventMath(event: ElementEvent.MathOperation) {
        mathOperation.invoke(event)
    }

    fun eventAddText(event: ElementEvent.AddText) {
        addText.invoke(event)
    }
}