package com.vlg.constructorinterface.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.domain.event.ActionExecutor
import com.vlg.constructorinterface.domain.event.EventDelegat
import com.vlg.constructorinterface.domain.event.MathExecutor
import com.vlg.constructorinterface.model.TableSchema
import com.vlg.constructorinterface.model.addText
import com.vlg.constructorinterface.model.setText
import com.vlg.constructorinterface.ui.createui.CreatorUI
import kotlin.random.Random

class RunFragment : BaseFragment() {

    private lateinit var workArea: LinearLayout
    private lateinit var executor: ActionExecutor
    private lateinit var eventDelegat: EventDelegat
    private var schema: TableSchema? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.run_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        schema = tableDataManager.loadTableSchema()

        workArea = view.findViewById(R.id.workArea)
        eventDelegat = EventDelegat()
        executor = ActionExecutor(eventDelegat)

        val mathExecutor = MathExecutor()

        eventDelegat.setOnShowToast {
            val substituted = mathExecutor.substituteVariablesView(it.message, creatorUI.getElementsMap())
            Toast.makeText(view.context, substituted, Toast.LENGTH_SHORT)
                .show()
        }

        eventDelegat.setOnShowDialog {
            val substitutedTitle = mathExecutor.substituteVariablesView(it.title, creatorUI.getElementsMap())
            val substitutedMessage = mathExecutor.substituteVariablesView(it.message, creatorUI.getElementsMap())
            AlertDialog.Builder(view.context)
                .setTitle(substitutedTitle)
                .setMessage(substitutedMessage)
                .setPositiveButton("OK", null)
                .show()
        }

        eventDelegat.setOnCreateEntry {
            val values = mutableMapOf<String, String>()
            uiManager.getListOfEditTexts().forEach { values.put(it.tag.toString(), it.text.toString()) }
            uiManager.getListOfSpinners().forEach { values.put(it.tag.toString(), it.selectedItem.toString()) }
            val uid = Random.Default.nextInt(1000000000) + Random.Default.nextInt(100000000)
            tableDataManager.saveTableSchema(tableDataManager.addNewRow(schema, uid, values))
        }

        eventDelegat.setOnDeleteEntry { }

        eventDelegat.setOnMathOperation {
            val substituted = mathExecutor.substituteVariablesView(it.expression, creatorUI.getElementsMap())
            if (Regex("\\b[a-zA-Z][a-zA-Z0-9_]*\\b").containsMatchIn(substituted)) {
                throw IllegalArgumentException("Unresolved variables in expression: $substituted")
            }
            val result = mathExecutor.calculate(substituted)
            creatorUI.getElementsMap()[it.resultTag]?.setText(result.toString())
        }

        eventDelegat.setOnAddText {
            val substituted = mathExecutor.substituteVariablesView(it.newText, creatorUI.getElementsMap())
            creatorUI.getElementsMap()[it.resultTag]?.addText(substituted)
        }

        eventDelegat.setOnOpenTable {
            val fragment = TableDataFragment()
            val bundle = Bundle()
            bundle.putString("TABLE_NAME", it.tableName)
            bundle.putString("PROJECT_PATH", projectPath)
            bundle.putString("PROJECT_NAME", projectPath)
            fragment.arguments = bundle
            fragment.arguments = bundle
            navigator.startFragment(fragment)
        }

        loadSavedLayout()
    }

    private fun loadSavedLayout() {
        val (layout, counter) = uiManager.loadLayoutFromFile()
        creatorUI.setElementCounter(counter)

        if (layout != null) {
            try {
                uiManager.restoreLayout(layout, workArea, executor = executor, actions = uiManager.getLayoutFileManager().loadActionsFromFile())
                Toast.makeText(mContext, "Интерфейс загружен из файла", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Log.e("LayoutLoad", "Error restoring layout", e)
                Toast.makeText(mContext, "Ошибка загрузки интерфейса", Toast.LENGTH_SHORT)
                    .show()
                creatorUI.addHintView(workArea)
            }
        } else {
            creatorUI.addHintView(workArea)
        }
    }
}