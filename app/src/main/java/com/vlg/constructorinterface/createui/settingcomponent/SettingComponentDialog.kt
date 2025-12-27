package com.vlg.constructorinterface.createui.settingcomponent

import android.content.Context
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
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.createui.CreatorUI
import com.vlg.constructorinterface.createui.ElementInfo
import com.vlg.constructorinterface.createui.customview.FakeSpinner
import com.vlg.constructorinterface.event.ActionType
import com.vlg.constructorinterface.event.ElementAction
import com.vlg.constructorinterface.table.TableDataManager

class SettingComponentDialog(
    private val context: Context,
    private val tableDataManager: TableDataManager
) {

    private lateinit var idEditText: EditText
    private lateinit var charIDCount: TextView
    private lateinit var editText: EditText
    private lateinit var charCount: TextView
    private lateinit var eventTypeSpinner: Spinner

    private lateinit var eventTypeUIHandler: EventTypeUIHandler
    private lateinit var actionBuilder: ActionBuilder
    private lateinit var textInputManager: TextInputManager
    private lateinit var componentTextUpdater: ComponentTextUpdater

    private var selectedActionType: ActionType = ActionType.NONE
    private var elementInfoList: List<ElementInfo> = emptyList()

    fun showDialog(
        layoutInflater: LayoutInflater,
        view: View,
        actions: MutableMap<String, ElementAction>
    ) {

        val currentText = when (view) {
            is TextView -> view.text.toString()
            is EditText -> view.hint.toString()
            is Button -> view.text.toString()
            is FakeSpinner -> view.text.toString()
            else -> ""
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_rename, null)
        initViews(dialogView)

        // Инициализация помощников
        eventTypeUIHandler = EventTypeUIHandler(context, dialogView)
        actionBuilder = ActionBuilder(context, tableDataManager)
        textInputManager = TextInputManager()
        componentTextUpdater = ComponentTextUpdater(context)

        eventTypeUIHandler.initViews()
        setupSpinner()
        setupTextWatchers(dialogView)

        // Настройка текстового поля
        textInputManager.setupTextWatchers(editText, charCount, currentText)
        textInputManager.setupTextWatchers(idEditText, charIDCount, view.tag.toString())
        charCount.text = textInputManager.getCurrentCharCountText(currentText)
        charIDCount.text = textInputManager.getCurrentCharCountText(currentText, 20)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Настройка компонента")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                handleSave(view, actions)
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
        idEditText.requestFocus()
    }

    private fun initViews(view: View) {
        idEditText = view.findViewById(R.id.renameIDEditText)
        charIDCount = view.findViewById(R.id.charIDCount)
        editText = view.findViewById(R.id.renameEditText)
        charCount = view.findViewById(R.id.charCount)
        eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner)
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
                Toast.makeText(context, position.toString(), Toast.LENGTH_SHORT).show()
                selectedActionType = ActionType.fromPosition(position) ?: ActionType.NONE
                Toast.makeText(context, selectedActionType.name, Toast.LENGTH_SHORT).show()
                updateUIForEventType()
            }

            // f07 edit
            // 138 text
            // 2dc button
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedActionType = ActionType.NONE
                updateUIForEventType()
            }
        }
    }

    private fun updateUIForEventType() {
        eventTypeUIHandler.updateUIForEventType(selectedActionType)

        if (selectedActionType == ActionType.MATH_OPERATION || selectedActionType == ActionType.ADD_TEXT) {
            elementInfoList = CreatorUI.getElementInfoList()
            eventTypeUIHandler.setupMathSpinner(elementInfoList)
        }
    }

    private fun setupTextWatchers(dialogView: View) {
        val textEditText: EditText = dialogView.findViewById(R.id.textEditText)
        val charCountDialog: TextView = dialogView.findViewById(R.id.charCountDialog)
        val textTitleEditText: EditText = dialogView.findViewById(R.id.textTitleEditText)
        val charCountDialogTitle: TextView = dialogView.findViewById(R.id.charCountDialogTitle)
        val textToastEditText: EditText = dialogView.findViewById(R.id.textToastEditText)
        val charCountToastTitle: TextView = dialogView.findViewById(R.id.charCountToastTitle)

        textInputManager.setupTextWatcher(textEditText, charCountDialog)
        textInputManager.setupTextWatcher(textTitleEditText, charCountDialogTitle)
        textInputManager.setupTextWatcher(textToastEditText, charCountToastTitle)
    }

    private fun handleSave(view: View, actions: MutableMap<String, ElementAction>) {
        val newText = editText.text.toString().trim()
        val newId = idEditText.text.toString().trim()

        if (!isValidTag(newId)) return

        componentTextUpdater.updateComponentID(view, newId, actions)
        componentTextUpdater.updateComponentText(view, newText)

        // Установка действия
        if (selectedActionType != ActionType.NONE) {
            setUpAction(view, actions)
            Toast.makeText(context, "Событие установлено", Toast.LENGTH_SHORT).show()
        }
    }


    fun isValidTag(tag: String?): Boolean {
        if (tag.isNullOrEmpty()) {
            Toast.makeText(context, "ID не должно быть пустым", Toast.LENGTH_SHORT).show()
            return false
        }

        if (tag.length >= 36) {
            Log.d("!!!", tag)
            Toast.makeText(context, "ID не должно быть больше 36", Toast.LENGTH_SHORT).show()
            return false
        }

        if (tag.contains(' ')) {
            Toast.makeText(context, "ID не должно содержать пробелы", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun setUpAction(view: View, actions: MutableMap<String, ElementAction>) {
        val tag = view.tag?.toString() ?: return

        val elementAction = when (selectedActionType) {
            ActionType.TOAST -> {
                actionBuilder.buildElementEvent(ActionType.TOAST, toastMessage = eventTypeUIHandler.getToastMessage())
            }

            ActionType.DIALOG -> {
                actionBuilder.buildElementEvent(
                    ActionType.DIALOG,
                    dialogTitle = eventTypeUIHandler.getDialogTitle(),
                    dialogMessage = eventTypeUIHandler.getDialogMessage()
                )
            }

            ActionType.CREATE_ENTRY,
            ActionType.OPEN_TABLE -> {
                actionBuilder.buildElementEvent(selectedActionType)
            }

            ActionType.MATH_OPERATION -> {
                val expression = eventTypeUIHandler.getMathExpression()
                val selectedPosition = eventTypeUIHandler.getSelectedResultSpinnerPosition()

                if (selectedPosition < 0 || selectedPosition >= elementInfoList.size) {
                    Toast.makeText(context, "Выберите элемент для результата", Toast.LENGTH_SHORT).show()
                    return
                }

                val selectedElement = elementInfoList[selectedPosition]
                actionBuilder.buildElementEvent(
                    ActionType.MATH_OPERATION,
                    expression = expression,
                    selectedElementInfo = selectedElement
                )
            }

            ActionType.ADD_TEXT -> {
                val expression = eventTypeUIHandler.getExpression()
                val selectedPosition = eventTypeUIHandler.getSelectedResultSpinnerPosition()

                if (selectedPosition < 0 || selectedPosition >= elementInfoList.size) {
                    Toast.makeText(context, "Выберите элемент для результата", Toast.LENGTH_SHORT).show()
                    return
                }

                val selectedElement = elementInfoList[selectedPosition]
                actionBuilder.buildElementEvent(
                    ActionType.ADD_TEXT,
                    expression = expression,
                    selectedElementInfo = selectedElement
                )
            }

            ActionType.CHANGE_TEXT -> {
                val newText = editText.text.toString()
                actionBuilder.buildElementEvent(ActionType.CHANGE_TEXT, newText = newText)
            }

            else -> null
        }

        elementAction?.let { event ->
            val existingAction = actions[tag]

            if (existingAction != null) {
                existingAction.events.add(event)
            } else {
                actions[tag] = ElementAction(mutableListOf(event), tag, "")
            }
            Log.d("SettingComponentDialog", "Добавлено действие для элемента $tag: ${selectedActionType.name}")
        }
    }
}