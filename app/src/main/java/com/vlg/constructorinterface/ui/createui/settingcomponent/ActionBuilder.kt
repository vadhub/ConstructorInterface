package com.vlg.constructorinterface.ui.createui.settingcomponent

import android.content.Context
import android.widget.Toast
import com.vlg.constructorinterface.model.ElementInfo
import com.vlg.constructorinterface.model.ActionType
import com.vlg.constructorinterface.model.ElementEvent
import com.vlg.constructorinterface.domain.table.TableDataManager

class ActionBuilder(
    private val context: Context,
    private val tableDataManager: TableDataManager
) {

    fun buildElementEvent(
        actionType: ActionType,
        toastMessage: String? = null,
        dialogTitle: String? = null,
        dialogMessage: String? = null,
        expression: String? = null,
        selectedElementInfo: ElementInfo? = null,
        newText: String? = null
    ): ElementEvent? {
        return when (actionType) {
            ActionType.TOAST -> {
                if (toastMessage.isNullOrEmpty()) {
                    Toast.makeText(context, "Введите текст тоста", Toast.LENGTH_SHORT).show()
                    null
                } else {
                    ElementEvent.ShowToast(toastMessage)
                }
            }

            ActionType.DIALOG -> {
                if (dialogTitle.isNullOrEmpty() || dialogMessage.isNullOrEmpty()) {
                    Toast.makeText(context, "Заполните все поля диалога", Toast.LENGTH_SHORT).show()
                    null
                } else {
                    ElementEvent.ShowDialog(dialogTitle, dialogMessage)
                }
            }

            ActionType.CREATE_ENTRY -> {
                val schema = tableDataManager.getListNamesTables()
                ElementEvent.CreateEntry(schema)
            }

            ActionType.OPEN_TABLE -> {
                val schema = tableDataManager.getListNamesTables()
                ElementEvent.OpenTable(schema)
            }

            ActionType.MATH_OPERATION -> {
                if (expression.isNullOrEmpty()) {
                    Toast.makeText(context, "Введите математическое выражение", Toast.LENGTH_SHORT).show()
                    null
                } else if (selectedElementInfo == null) {
                    Toast.makeText(context, "Выберите элемент для результата", Toast.LENGTH_SHORT).show()
                    null
                } else {
                    ElementEvent.MathOperation(
                        expression = expression,
                        resultTag = selectedElementInfo.tag
                    )
                }
            }

            ActionType.ADD_TEXT -> {
                if (expression.isNullOrEmpty()) {
                    Toast.makeText(context, "Введите текст", Toast.LENGTH_SHORT).show()
                    null
                } else if (selectedElementInfo == null) {
                    Toast.makeText(context, "Выберите элемент для результата", Toast.LENGTH_SHORT).show()
                    null
                } else {
                    ElementEvent.AddText(
                        newText = expression,
                        resultTag = selectedElementInfo.tag
                    )
                }
            }

            ActionType.CHANGE_TEXT -> {
                if (newText.isNullOrEmpty()) {
                    Toast.makeText(context, "Введите новый текст", Toast.LENGTH_SHORT).show()
                    null
                } else {
                    ElementEvent.ChangeText(newText)
                }
            }

            else -> null
        }
    }
}