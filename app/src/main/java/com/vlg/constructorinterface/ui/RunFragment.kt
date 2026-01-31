package com.vlg.constructorinterface.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.domain.event.ActionExecutor
import com.vlg.constructorinterface.domain.event.EventDelegate
import com.vlg.constructorinterface.domain.event.MathExecutor
import com.vlg.constructorinterface.model.TableSchema
import kotlin.random.Random

class RunFragment : BaseFragment() {

    private lateinit var workArea: LinearLayout
    private lateinit var executor: ActionExecutor
    private lateinit var eventDelegate: EventDelegate
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
        eventDelegate = EventDelegate()
        executor = ActionExecutor(eventDelegate)

        val mathExecutor = MathExecutor()

        eventDelegate.setOnShowToast {
            val substituted = mathExecutor.substituteVariablesView(it.message, creatorUI.getElementsMap().map { (_, value) -> value })
            Toast.makeText(view.context, substituted, Toast.LENGTH_SHORT)
                .show()
        }

        eventDelegate.setOnShowDialog {
            val substitutedTitle = mathExecutor.substituteVariablesView(it.title, creatorUI.getElementsMap().map { (_, value) -> value })
            val substitutedMessage = mathExecutor.substituteVariablesView(it.message, creatorUI.getElementsMap().map { (_, value) -> value })
            AlertDialog.Builder(view.context)
                .setTitle(substitutedTitle)
                .setMessage(substitutedMessage)
                .setPositiveButton("OK", null)
                .show()
        }

        eventDelegate.setOnCreateEntry {
            val values = mutableMapOf<String, String>()
            uiManager.getListOfEditTexts().forEach { values.put(it.tag.toString(), it.text.toString()) }
            uiManager.getListOfSpinners().forEach { values.put(it.tag.toString(), it.selectedItem.toString()) }
            val uid = Random.Default.nextInt(1000000000) + Random.Default.nextInt(100000000)
            tableDataManager.saveTableSchema(tableDataManager.addNewRow(schema, uid, values))
        }

        eventDelegate.setOnDeleteEntry { }

        eventDelegate.setOnMathOperation {
            val substituted = mathExecutor.substituteVariablesView(it.expression, creatorUI.getElementsMap().map { (_, value) -> value })
            Log.d("!!!", creatorUI.getElementsMap().toString())
            Log.d("!!!", substituted)
            if (Regex("\\b[a-zA-Z][a-zA-Z0-9_]*\\b").containsMatchIn(substituted)) {
                throw IllegalArgumentException("Unresolved variables in expression: $substituted")
            }
            val result = mathExecutor.calculate(substituted)
            val textView = workArea.findViewById<TextView>(it.idResult?:-1)
            textView?.text = result.toString()

        }

        eventDelegate.setOnAddText {
            val substituted = mathExecutor.substituteVariablesView(it.newText, creatorUI.getElementsMap().map { (_, value) -> value })
            val textView = workArea.findViewById<TextView>(it.idResult?:-1)
            val text = textView?.text
            textView.text = text.toString()+substituted
        }

        eventDelegate.setOnOpenTable {
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