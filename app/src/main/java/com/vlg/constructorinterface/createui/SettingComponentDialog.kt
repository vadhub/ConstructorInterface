package com.vlg.constructorinterface.createui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.vlg.constructorinterface.event.ElementAction
import com.vlg.constructorinterface.event.ElementEvent
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.table.TableDataManager

class SettingComponentDialog(
    private val context: Context,
    private val tableDataManager: TableDataManager
) {

    private lateinit var eventTypeSpinner: Spinner
    private lateinit var renameEditText: EditText
    private lateinit var charCount: TextView
    private lateinit var textEditText: EditText
    private lateinit var textTitleEditText: EditText
    private lateinit var textToastEditText: EditText
    private lateinit var charCountDialog: TextView
    private lateinit var charCountDialogTitle: TextView
    private lateinit var charCountToastTitle: TextView
    private lateinit var dialogTextLabel: TextView
    private lateinit var dialogTitleLabel: TextView
    private lateinit var toastTextLabel: TextView

    private var selectedActionType: ActionType? = null

    fun showDialog(
        layoutInflater: LayoutInflater,
        view: View,
        actions: MutableMap<String, ElementAction>
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

        renameEditText.setText(currentText)
        renameEditText.setSelection(currentText.length)
        updateCharCount(renameEditText, charCount)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Переименование")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                handleSaveClick(view, actions)
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
        renameEditText.requestFocus()
    }

    private fun initViews(view: View) {
        renameEditText = view.findViewById(R.id.renameEditText)
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
        ArrayAdapter.createFromResource(
            context,
            R.array.event_types,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            eventTypeSpinner.adapter = this
        }

        eventTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedActionType = ActionType.fromPosition(position)
                updateUIForEventType(selectedActionType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedActionType = null
                hideAllEventFields()
            }
        }
    }

    private fun updateUIForEventType(type: ActionType?) {
        hideAllEventFields()

        when (type) {
            ActionType.TOAST -> {
                toastTextLabel.visibility = View.VISIBLE
                textToastEditText.visibility = View.VISIBLE
                charCountToastTitle.visibility = View.VISIBLE
            }
            ActionType.DIALOG -> {
                dialogTextLabel.visibility = View.VISIBLE
                textEditText.visibility = View.VISIBLE
                charCountDialog.visibility = View.VISIBLE
                dialogTitleLabel.visibility = View.VISIBLE
                textTitleEditText.visibility = View.VISIBLE
                charCountDialogTitle.visibility = View.VISIBLE
            }
            ActionType.CREATE_ENTRY, ActionType.OPEN_TABLE -> Unit
            else -> Unit
        }
    }

    private fun handleSaveClick(view: View, actions: MutableMap<String, ElementAction>) {
        val newText = renameEditText.text.toString().trim()

        if (newText.isNotEmpty()) {
            when (view) {
                is EditText -> view.hint = newText
                is TextView -> view.text = newText
                is Button -> view.text = newText
                is Spinner -> view.adapter = CreatorUI.createAdapterSpinner(context, newText)
            }
            Toast.makeText(context, "Текст изменён", Toast.LENGTH_SHORT).show()
        }

        selectedActionType?.let { actionType ->
            view.tag?.toString()?.let { tag ->
                try {
                    val elementAction = createElementAction(actionType, tag)
                    actions[tag] = elementAction
                    Toast.makeText(context, "Событие установлено", Toast.LENGTH_SHORT).show()
                    Log.d("SettingComponentDialog", "Действие установлено для тега: $tag")
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Ошибка при создании действия: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("SettingComponentDialog", "Ошибка: $e")
                }
            } ?: run {
                Toast.makeText(context, "Компонент не имеет тега", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createElementAction(type: ActionType, tag: String): ElementAction {
        return when (type) {
            ActionType.TOAST -> ElementAction(
                mutableListOf(ElementEvent.ShowToast(textToastEditText.text.toString())),
                tag,
                ""
            )
            ActionType.DIALOG -> ElementAction(
                mutableListOf(
                    ElementEvent.ShowDialog(
                        textTitleEditText.text.toString(),
                        textEditText.text.toString()
                    )
                ),
                tag,
                ""
            )
            ActionType.CREATE_ENTRY -> ElementAction(
                mutableListOf(ElementEvent.CreateEntry(tableDataManager.getListNamesTables())),
                tag,
                ""
            )
            ActionType.OPEN_TABLE -> ElementAction(
                mutableListOf(ElementEvent.OpenTable(tableDataManager.getListNamesTables())),
                tag,
                ""
            )
            else -> throw IllegalArgumentException("Неподдерживаемый тип действия: $type")
        }
    }


    private fun hideAllEventFields() {
        listOf(
            dialogTextLabel, textEditText, charCountDialog,
            dialogTitleLabel, textTitleEditText, charCountDialogTitle,
            toastTextLabel, textToastEditText, charCountToastTitle
        ).forEach { it.visibility = View.GONE }
    }

    private fun setupTextWatchers() {
        textEditText.doOnTextChanged { text, _, _, _ ->
            charCountDialog.text = "${text?.length ?: 0}/50"
        }

        textTitleEditText.doOnTextChanged { text, _, _, _ ->
            charCountDialogTitle.text = "${text?.length ?: 0}/50"
        }

        textToastEditText.doOnTextChanged { text, _, _, _ ->
            charCountToastTitle.text = "${text?.length ?: 0}/50"
        }
    }

    private fun updateCharCount(editText: EditText, counterView: TextView) {
        val text = editText.text
        counterView.text = "${text?.length ?: 0}/50"
    }
}

enum class ActionType(val position: Int) {
    NONE(0),
    TOAST(1),
    DIALOG(2),
    CREATE_ENTRY(3),
    OPEN_TABLE(4);

    companion object {
        fun fromPosition(position: Int): ActionType? {
            return values().find { it.position == position }
        }
    }
}