package com.vlg.constructorinterface

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.vlg.constructorinterface.createui.CreatorUI
import com.vlg.constructorinterface.createui.UIManager
import com.vlg.constructorinterface.createui.addText
import com.vlg.constructorinterface.createui.setText
import com.vlg.constructorinterface.event.ActionExecutor
import com.vlg.constructorinterface.event.EventDelegat
import com.vlg.constructorinterface.event.MathExecutor
import com.vlg.constructorinterface.table.TableDataManager
import com.vlg.constructorinterface.table.TableSchema
import kotlin.random.Random

class RunFragment : Fragment() {

    private lateinit var workArea: LinearLayout
    private lateinit var creatorUI: CreatorUI
    private lateinit var uiManager: UIManager
    private lateinit var executor: ActionExecutor
    private lateinit var eventDelegat: EventDelegat
    private lateinit var tableDataManager: TableDataManager
    private var schema: TableSchema? = null
    private lateinit var navigator: Navigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator = context as Navigator
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.run_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tableDataManager = TableDataManager(view.context)
        schema = tableDataManager.loadTableSchema()

        workArea = view.findViewById(R.id.workArea)
        creatorUI = CreatorUI(view.context, layoutInflater)
        uiManager = UIManager(view.context, creatorUI)
        eventDelegat = EventDelegat()
        executor = ActionExecutor(eventDelegat)

        val mathExecutor = MathExecutor()

        eventDelegat.setOnShowToast {
            val substituted = mathExecutor.substituteVariablesView(it.message, CreatorUI.getElementsMap())
            Toast.makeText(view.context, substituted, Toast.LENGTH_SHORT)
                .show()
        }

        eventDelegat.setOnShowDialog {
            val substitutedTitle = mathExecutor.substituteVariablesView(it.title, CreatorUI.getElementsMap())
            val substitutedMessage = mathExecutor.substituteVariablesView(it.message, CreatorUI.getElementsMap())
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
            val uid = Random.nextInt(1000000000) + Random.nextInt(100000000)
            tableDataManager.saveTableSchema(tableDataManager.addNewRow(schema, uid, values))
        }

        eventDelegat.setOnDeleteEntry { }

        eventDelegat.setOnMathOperation {
            val substituted = mathExecutor.substituteVariablesView(it.expression, CreatorUI.getElementsMap())
            if (Regex("\\b[a-zA-Z][a-zA-Z0-9_]*\\b").containsMatchIn(substituted)) {
                throw IllegalArgumentException("Unresolved variables in expression: $substituted")
            }
            val result = mathExecutor.calculate(substituted)
            CreatorUI.getElementsMap()[it.resultTag]?.setText(result.toString())
        }

        eventDelegat.setOnAddText {
            val substituted = mathExecutor.substituteVariablesView(it.newText, CreatorUI.getElementsMap())
            CreatorUI.getElementsMap()[it.resultTag]?.addText(substituted)
        }

        eventDelegat.setOnOpenTable {
            val fragment = TableDataFragment()
            val bundle = Bundle()
            bundle.putString("TABLE_NAME", it.tableName)
            fragment.arguments = bundle
            navigator.startFragment(fragment)
        }

        loadSavedLayout(view)
    }

    private fun loadSavedLayout(view: View) {
        val (layout, counter) = uiManager.loadLayoutFromFile()
        creatorUI.setElementCounter(counter)

        if (layout != null) {
            try {
                uiManager.restoreLayout(layout, workArea, executor = executor, actions = uiManager.getLayoutFileManager().loadActionsFromFile())
                Toast.makeText(view.context, "Интерфейс загружен из файла", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Log.e("LayoutLoad", "Error restoring layout", e)
                Toast.makeText(view.context, "Ошибка загрузки интерфейса", Toast.LENGTH_SHORT)
                    .show()
                creatorUI.addHintView(workArea)
            }
        } else {
            creatorUI.addHintView(workArea)
        }
    }
}