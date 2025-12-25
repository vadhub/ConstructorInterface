package com.vlg.constructorinterface

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vlg.constructorinterface.createui.CreatorUI
import com.vlg.constructorinterface.createui.UIManager
import com.vlg.constructorinterface.event.ActionExecutor
import com.vlg.constructorinterface.event.EventDelegat
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
        eventDelegat = EventDelegat(view.context)
        executor = ActionExecutor(eventDelegat)

        eventDelegat.setOnCreateEntry {
            Log.d("!!!setOnCreateEntry", "start save")
            val values = mutableMapOf<String, String>()
            uiManager.getListOfEditTexts().forEach { values.put(it.tag.toString(), it.text.toString()) }
            uiManager.getListOfEditTexts().forEach { Log.d("!! !fff", it.text.toString()) }
            val uid = Random.nextInt(1000000000) + Random.nextInt(100000000)
            Log.d("!!!setOnCreateEntry", "$uid $values")
            val success = tableDataManager.saveTableSchema(tableDataManager.addNewRow(schema, uid, values))
            Log.d("!!!setOnCreateEntry", "end save $success")
        }

        eventDelegat.setOnDeleteEntry { }

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
                Log.d("!!!loadActionsFromFile", uiManager.getLayoutFileManager().loadActionsFromFile().toString())
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