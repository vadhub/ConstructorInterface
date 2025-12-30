package com.vlg.constructorinterface.ui.createui.settingcomponent

import android.widget.EditText
import android.widget.TextView
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
}