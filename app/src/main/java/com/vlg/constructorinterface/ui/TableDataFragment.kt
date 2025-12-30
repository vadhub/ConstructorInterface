package com.vlg.constructorinterface.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.model.getFormattedTableAligned
import com.vlg.constructorinterface.domain.table.TableDataManager

class TableDataFragment : Fragment() {

    private lateinit var table: TextView
    private lateinit var tableDataManager: TableDataManager

    private val projectPath by lazy { arguments?.getString("PROJECT_PATH") }
    private val projectName by lazy { arguments?.getString("PROJECT_NAME") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.table_data_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tableDataManager = TableDataManager(view.context, projectPath ?: "")
        val schema = tableDataManager.loadTableSchema()
        table = view.findViewById(R.id.table)
        table.text = schema?.getFormattedTableAligned()
    }

}