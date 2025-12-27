package com.vlg.constructorinterface.event

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class EventDelegat(private val context: Context) {

    private var createEntry: (ElementEvent.CreateEntry) -> Unit = {}
    private var deleteEntry: (ElementEvent.DeleteEntry) -> Unit = {}
    private var openTable: (ElementEvent.OpenTable) -> Unit = {}
    private var mathOperation: (ElementEvent.MathOperation) -> Unit = {}
    private var addText: (ElementEvent.AddText) -> Unit = {}

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

    fun eventToast(event: ElementEvent.ShowToast) {
        Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
            .show()
    }

    fun eventDialog(event: ElementEvent.ShowDialog) {
        AlertDialog.Builder(context)
            .setTitle(event.title)
            .setMessage(event.message)
            .setPositiveButton("OK", null)
            .show()
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