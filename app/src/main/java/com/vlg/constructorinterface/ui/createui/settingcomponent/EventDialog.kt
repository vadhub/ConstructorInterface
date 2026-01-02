package com.vlg.constructorinterface.ui.createui.settingcomponent

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.model.ActionType
import com.vlg.constructorinterface.model.Element
import com.vlg.constructorinterface.model.ElementAction
import com.vlg.constructorinterface.ui.createui.CreatorUI

class EventDialog(private val viewId: Int, private val creatorUI: CreatorUI) :
    DialogFragment() {

    private lateinit var eventTypeSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var eventTypeUIHandler: EventTypeUIHandler
    private lateinit var textInputManager: TextInputManager
    private var selectedActionType: ActionType = ActionType.NONE
    private var elementInfoList: List<Element> = emptyList()
    private lateinit var actionBuilder: ActionBuilder

    private var onCompleteListener: OnCompleteListener? = null

    interface OnCompleteListener {
        fun onSaved()
        fun onCancelled()
    }

    fun setOnSettingCompleteListener(listener: OnCompleteListener) {
        this.onCompleteListener = listener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.event_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        eventTypeSpinner = view.findViewById(R.id.eventTypeSpinner)
        eventTypeUIHandler = EventTypeUIHandler(requireContext(), view)
        textInputManager = TextInputManager()
        actionBuilder = ActionBuilder(requireContext(), creatorUI.tableDataManager)

        setupSpinner()
        setupButtons()
        setupTextWatchers(view)
        eventTypeUIHandler.initViews()

        elementInfoList = creatorUI.getElementsMap().map { (_, value) -> value }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle("Настройка события")
        return dialog
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
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
                selectedActionType = ActionType.fromPosition(position) ?: ActionType.NONE
                updateUIForEventType()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedActionType = ActionType.NONE
                updateUIForEventType()
            }
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

    private fun updateUIForEventType() {
        eventTypeUIHandler.updateUIForEventType(selectedActionType)

        if (selectedActionType == ActionType.MATH_OPERATION || selectedActionType == ActionType.ADD_TEXT) {
            eventTypeUIHandler.setupMathSpinner(elementInfoList)
        }
    }

    private fun setupButtons() {
        saveButton.setOnClickListener {
            buildElementAction()?.let {
                creatorUI.addOrUpdateElementAction(viewId, it)
            }
            onCompleteListener?.onSaved()
            dismiss()
        }

        cancelButton.setOnClickListener {
            onCompleteListener?.onCancelled()
            dismiss()
        }
    }

    private fun buildElementAction(): ElementAction? {
        val event = when (selectedActionType) {
            ActionType.TOAST -> {
                actionBuilder.buildElementEvent(
                    ActionType.TOAST,
                    toastMessage = eventTypeUIHandler.getToastMessage()
                )
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
                    Toast.makeText(
                        requireContext(),
                        "Выберите элемент для результата",
                        Toast.LENGTH_SHORT
                    ).show()
                    return null
                }

                val selectedElement = elementInfoList[selectedPosition]
                actionBuilder.buildElementEvent(
                    ActionType.MATH_OPERATION,
                    expression = expression,
                    selectedElement = selectedElement
                )
            }

            ActionType.ADD_TEXT -> {
                val expression = eventTypeUIHandler.getExpression()
                val selectedPosition = eventTypeUIHandler.getSelectedResultSpinnerPosition()

                if (selectedPosition < 0 || selectedPosition >= elementInfoList.size) {
                    Toast.makeText(
                        requireContext(),
                        "Выберите элемент для результата",
                        Toast.LENGTH_SHORT
                    ).show()
                    return null
                }

                val selectedElement = elementInfoList[selectedPosition]
                actionBuilder.buildElementEvent(
                    ActionType.ADD_TEXT,
                    expression = expression,
                    selectedElement = selectedElement
                )
            }

            ActionType.CHANGE_TEXT -> {
                val newText = eventTypeUIHandler.getExpression()
                actionBuilder.buildElementEvent(ActionType.CHANGE_TEXT, newText = newText)
            }

            else -> null
        }

        return event?.let { ElementAction(mutableListOf(it), viewId, "") }
    }
}