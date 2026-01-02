package com.vlg.constructorinterface.ui.createui.settingcomponent

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.model.ActionType
import com.vlg.constructorinterface.model.Element

class EventTypeUIHandler(
    private val context: Context,
    private val rootView: View
) {

    private lateinit var dialogTextLabel: TextView
    private lateinit var textEditText: EditText
    private lateinit var charCountDialog: TextView
    private lateinit var dialogTitleLabel: TextView
    private lateinit var textTitleEditText: EditText
    private lateinit var charCountDialogTitle: TextView
    private lateinit var toastTextLabel: TextView
    private lateinit var textToastEditText: EditText
    private lateinit var charCountToastTitle: TextView

    private lateinit var mathLabel: TextView
    private lateinit var expressionInput: EditText
    private lateinit var resultSpinner: Spinner
    private lateinit var previewText: TextView

    private lateinit var addTextLabel: TextView
    private lateinit var expression: EditText
    private lateinit var saveToLabel: TextView

    fun initViews() {
        dialogTextLabel = rootView.findViewById(R.id.titleTextDialog)
        textEditText = rootView.findViewById(R.id.textEditText)
        charCountDialog = rootView.findViewById(R.id.charCountDialog)
        dialogTitleLabel = rootView.findViewById(R.id.titleDialog)
        textTitleEditText = rootView.findViewById(R.id.textTitleEditText)
        charCountDialogTitle = rootView.findViewById(R.id.charCountDialogTitle)
        toastTextLabel = rootView.findViewById(R.id.titleToast)
        textToastEditText = rootView.findViewById(R.id.textToastEditText)
        charCountToastTitle = rootView.findViewById(R.id.charCountToastTitle)

        mathLabel = rootView.findViewById(R.id.mathLabel)
        expressionInput = rootView.findViewById(R.id.math_expression)
        resultSpinner = rootView.findViewById(R.id.result_tag_spinner)
        previewText = rootView.findViewById(R.id.preview_text)

        addTextLabel = rootView.findViewById(R.id.addTextLabel)
        expression = rootView.findViewById(R.id._expression)
        saveToLabel = rootView.findViewById(R.id.labelSaveTo)
    }

    fun updateUIForEventType(actionType: ActionType) {
        hideAllEventFields()

        when (actionType) {
            ActionType.TOAST -> {
                toastTextLabel.visibility = View.VISIBLE
                textToastEditText.visibility = View.VISIBLE
                charCountToastTitle.visibility = View.VISIBLE
                hideMathOperationUI()
            }

            ActionType.DIALOG -> {
                dialogTextLabel.visibility = View.VISIBLE
                textEditText.visibility = View.VISIBLE
                charCountDialog.visibility = View.VISIBLE
                dialogTitleLabel.visibility = View.VISIBLE
                textTitleEditText.visibility = View.VISIBLE
                charCountDialogTitle.visibility = View.VISIBLE
                hideMathOperationUI()
            }

            ActionType.MATH_OPERATION -> {
                setupMathOperationUI()
            }

            ActionType.ADD_TEXT -> {
                setupAddTextUI()
            }

            else -> {
                // Для остальных типов событий не показываем дополнительные поля
            }
        }
    }

    private fun setupAddTextUI() {
        addTextLabel.visibility = View.VISIBLE
        expression.visibility = View.VISIBLE
        saveToLabel.visibility = View.VISIBLE
        resultSpinner.visibility = View.VISIBLE
    }

    private fun setupMathOperationUI() {
        mathLabel.visibility = View.VISIBLE
        expressionInput.visibility = View.VISIBLE
        saveToLabel.visibility = View.VISIBLE
        resultSpinner.visibility = View.VISIBLE
        previewText.visibility = View.VISIBLE
    }

    private fun hideMathOperationUI() {
        mathLabel.visibility = View.GONE
        expressionInput.visibility = View.GONE
        resultSpinner.visibility = View.GONE
        saveToLabel.visibility = View.GONE
        previewText.visibility = View.GONE
    }

    fun setupMathSpinner(elementInfoList: List<Element>) {
        val adapter = createCustomAdapter(elementInfoList)
        resultSpinner.adapter = adapter
    }

    private fun createCustomAdapter(elementInfoList: List<Element>): ArrayAdapter<Element> {
        return object : ArrayAdapter<Element>(
            context,
            android.R.layout.simple_spinner_item,
            elementInfoList
        ) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                val elementInfo = elementInfoList[position]
                view.text = elementInfo.type.name
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                val elementInfo = elementInfoList[position]
                view.text = "${elementInfo.type.name}\nТекущее значение: ${elementInfo.text + "" + elementInfo.hint}"
                view.maxLines = 2
                view.setPadding(16, 16, 16, 16)
                return view
            }
        }
    }

    fun getMathExpression(): String = expressionInput.text.toString()

    fun getExpression(): String = expression.text.toString()

    fun getSelectedResultSpinnerPosition(): Int = resultSpinner.selectedItemPosition

    fun getToastMessage(): String = textToastEditText.text.toString()

    fun getDialogTitle(): String = textTitleEditText.text.toString()

    fun getDialogMessage(): String = textEditText.text.toString()

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

        mathLabel.visibility = View.GONE
        expressionInput.visibility = View.GONE
        resultSpinner.visibility = View.GONE
        previewText.visibility = View.GONE

        addTextLabel.visibility = View.GONE
        expression.visibility = View.GONE
        saveToLabel.visibility = View.GONE
    }
}