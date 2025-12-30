package com.vlg.constructorinterface.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.vlg.constructorinterface.Navigator
import com.vlg.constructorinterface.domain.table.TableDataManager
import com.vlg.constructorinterface.ui.createui.CreatorUI
import com.vlg.constructorinterface.ui.createui.UIManager

open class BaseFragment : Fragment() {
    protected lateinit var creatorUI: CreatorUI
    protected lateinit var uiManager: UIManager

    protected lateinit var mContext: Context
    protected lateinit var navigator: Navigator

    protected lateinit var tableDataManager: TableDataManager

    protected val projectPath by lazy { arguments?.getString("PROJECT_PATH") }
    protected val projectName by lazy { arguments?.getString("PROJECT_NAME") }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        navigator = context as Navigator
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tableDataManager = TableDataManager(mContext, projectPath ?: "")
        creatorUI = CreatorUI(mContext, navigator, tableDataManager)
        uiManager = UIManager(mContext, creatorUI, projectPath ?: "")
    }

}