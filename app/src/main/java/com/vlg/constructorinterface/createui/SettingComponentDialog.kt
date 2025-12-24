package com.vlg.constructorinterface.createui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import com.vlg.constructorinterface.ActionWrite
import com.vlg.constructorinterface.ElementAction
import com.vlg.constructorinterface.ElementEvent
import com.vlg.constructorinterface.R

class SettingComponentDialog(private val context: Context, private val actionWrite: ActionWrite) {

    private lateinit var eventTypeSpinner: Spinner
    private lateinit var textEditText: EditText
    private lateinit var editText: EditText
    private lateinit var textTitleEditText: EditText
    private lateinit var textToastEditText: EditText
    private lateinit var charCountDialog: TextView
    private lateinit var charCount: TextView
    private lateinit var charCountDialogTitle: TextView
    private lateinit var charCountToastTitle: TextView

    private lateinit var dialogTextLabel: TextView
    private lateinit var dialogTitleLabel: TextView
    private lateinit var toastTextLabel: TextView
    private var action = 0

    fun showDialog(
        layoutInflater: LayoutInflater,
        view: View,
        actions: MutableList<ElementAction>
    ) {
        val currentText = when (view) {
            is TextView -> view.text.toString()
            is EditText -> view.hint.toString()
            is Button -> view.text.toString()
            else -> ""
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_rename, null)
        initViews(dialogView)
        setupSpinner()
        setupTextWatchers()

        editText.setText(currentText)
        editText.setSelection(currentText.length)

        editText.doOnTextChanged { text, _, _, _ ->
            val count = text?.length ?: 0
            charCount.text = "$count/50"
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle("Переименование")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    when (view) {
                        is EditText -> view.hint = newText
                        is TextView -> view.text = newText
                        is Button -> view.text = newText
                    }
                    Toast.makeText(context, "Текст изменен", Toast.LENGTH_SHORT).show()
                }

                if (action != 0) {

                    setUpAction(action, view, actions)
                    Toast.makeText(context, "Событие установлено", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()

        charCount.text = "${currentText.length}/50"
        editText.requestFocus()
    }

    private fun initViews(view: View) {
        editText = view.findViewById(R.id.renameEditText)
        charCount = view.findViewById(R.id.charCount)
        eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner)
        textEditText = view.findViewById(R.id.textEditText)
        textTitleEditText = view.findViewById(R.id.textTitleEditText)
        textToastEditText = view.findViewById(R.id.textToastEditText)
        charCountDialog = view.findViewById(R.id.charCountDialog)
        charCountDialogTitle = view.findViewById(R.id.charCountDialogTitle)
        charCountToastTitle = view.findViewById(R.id.charCountToastTitle)
        dialogTextLabel = view.findViewById(R.id.titleTextDialog)
        dialogTitleLabel = view.findViewById(R.id.titleDialog)
        toastTextLabel = view.findViewById(R.id.titleToast)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            context,
            R.array.event_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        eventTypeSpinner.adapter = adapter

        eventTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                action = position
                updateUIForEventType(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun updateUIForEventType(
        position: Int,
    ) {
        hideAllEventFields()

        when (position) {
            0 -> { // none
            }

            1 -> { // toast
                toastTextLabel.visibility = View.VISIBLE
                textToastEditText.visibility = View.VISIBLE
                charCountToastTitle.visibility = View.VISIBLE
            }

            2 -> { // dialog
                dialogTextLabel.visibility = View.VISIBLE
                textEditText.visibility = View.VISIBLE
                charCountDialog.visibility = View.VISIBLE
                dialogTitleLabel.visibility = View.VISIBLE
                textTitleEditText.visibility = View.VISIBLE
                charCountDialogTitle.visibility = View.VISIBLE
            }

            3 -> { // write
                hideAllEventFields()
            }
        }
    }

    private fun setUpAction(type: Int, view: View, actions: MutableList<ElementAction>) {
        when (type) {
            0 -> {

            }

            1 -> {
                actions.add(
                    ElementAction(
                        ElementEvent.ShowToast(textToastEditText.text.toString()),
                        view.tag.toString(),
                        ""
                    )
                )
            }

            2 -> {
                actions.add(
                    ElementAction(
                        ElementEvent.ShowDialog(
                            dialogTitleLabel.text.toString(),
                            textEditText.text.toString()
                        ), view.tag.toString(), ""
                    )
                )
            }
            3 -> actionWrite.onWrite()
        }
    }

    private fun hideAllEventFields() {
        dialogTextLabel.visibility = View.GONE
        textEditText.visibility = View.GONE
        charCountDialog.visibility = View.GONE
        dialogTitleLabel.visibility = View.GONE
        textTitleEditText.visibility = View.GONE
        charCountDialogTitle.visibility = View.GONE
        toastTextLabel.visibility = View.GONE
        textToastEditText.visibility = View.GONE
        charCountToastTitle.visibility = View.GONE
    }

    private fun setupTextWatchers() {
        textEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                charCountDialog.text = "${s?.length ?: 0}/50"
            }
        })

        textTitleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                charCountDialogTitle.text = "${s?.length ?: 0}/50"
            }
        })

        textToastEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                charCountToastTitle.text = "${s?.length ?: 0}/50"
            }
        })
    }
}