package com.vlg.constructorinterface

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
import org.json.JSONArray
import java.io.File

class RunFragment : Fragment() {

    private lateinit var workArea: LinearLayout
    private lateinit var creatorUI: CreatorUI
    private lateinit var uiManager: UIManager
    private lateinit var executor: ActionExecutor
    private lateinit var tableDataManager: TableDataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.run_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        workArea = view.findViewById(R.id.workArea)
        creatorUI = CreatorUI(view.context, layoutInflater)
        uiManager = UIManager(view.context, creatorUI)
        tableDataManager = TableDataManager(view.context)
        executor = ActionExecutor(view.context, tableDataManager)
//        executor.execute()
//
        loadSavedLayout(view)
    }

    private fun loadSavedLayout(view: View) {
        val (layout, counter) = uiManager.loadLayoutFromFile()
        creatorUI.setElementCounter(counter)

        if (layout != null) {
            try {
                uiManager.restoreLayout(layout, workArea)
                Toast.makeText(view.context, "Интерфейс загружен из файла", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("LayoutLoad", "Error restoring layout", e)
                Toast.makeText(view.context, "Ошибка загрузки интерфейса", Toast.LENGTH_SHORT).show()
                creatorUI.addHintView(workArea)
            }
        } else {
            creatorUI.addHintView(workArea)
        }
    }

    fun loadActionsFromFile(file: File): List<ElementAction> {
        return try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)
            jsonArray.toElementActionList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}