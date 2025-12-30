package com.vlg.constructorinterface.ui.createui.settingcomponent

import android.content.Context
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged

class TextInputManager {

    fun setupTextWatchers(
        editText: EditText,
        charCountTextView: TextView,
        initialText: String
    ) {
        editText.setText(initialText)
        editText.setSelection(initialText.length)

        editText.doOnTextChanged { text, _, _, _ ->
            val count = text?.length ?: 0
            charCountTextView.text = "$count/50"
        }
    }

    fun setupTextWatcher(
        editText: EditText,
        charCountTextView: TextView,
        maxLength: Int = 50
    ) {
        editText.doOnTextChanged { text, _, _, _ ->
            val count = text?.length ?: 0
            charCountTextView.text = "$count/$maxLength"
        }
    }

    fun getCurrentCharCountText(text: String, maxLength: Int = 50): String {
        return "${text.length}/$maxLength"
    }

    fun isValidTag(context: Context, tag: String?, maxLength: Int = 50): Boolean {
        if (tag.isNullOrEmpty()) {
            Toast.makeText(context, "ID не должно быть пустым", Toast.LENGTH_SHORT).show()
            return false
        }

        if (tag.length >= maxLength) {
            Toast.makeText(context, "ID не должно быть больше $maxLength", Toast.LENGTH_SHORT).show()
            return false
        }

        if (tag.contains(' ')) {
            Toast.makeText(context, "ID не должно содержать пробелы", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}