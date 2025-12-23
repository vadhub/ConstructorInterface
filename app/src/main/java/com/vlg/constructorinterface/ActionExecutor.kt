package com.vlg.constructorinterface

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ActionExecutor(private val context: Context) {
    fun execute(event: ElementEvent) {
        when (event) {
            is ElementEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            is ElementEvent.ShowDialog -> AlertDialog.Builder(context)
                .setTitle(event.title)
                .setMessage(event.message)
                .setPositiveButton("OK", null)
                .show()
            else -> ""
        }
    }
}