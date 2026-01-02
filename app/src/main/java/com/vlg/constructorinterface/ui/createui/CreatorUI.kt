package com.vlg.constructorinterface.ui.createui

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.vlg.constructorinterface.domain.table.TableDataManager
import com.vlg.constructorinterface.model.ElementAction


class CreatorUI(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    val tableDataManager: TableDataManager
) {

    val elementFactory = ElementFactory(context)
    val elementManager = ElementManager()
    val workAreaManager = WorkAreaManager(context, elementManager)
    private val dropManager = DropManager(context, workAreaManager)
    val eventActionManager = EventActionManager()
    val uiInteractionManager = UIInteractionManager(
        context,
        fragmentManager,
        elementManager,
        eventActionManager,
        this
    )

    fun getElementsMap() = elementManager.getElementsMap()
    fun setAction(actions: MutableMap<Int, ElementAction>) = eventActionManager.setActions(actions)
    fun getElementCounter() = elementManager.getElementCounter()
    fun setElementCounter(i: Int) = elementManager.setElementCounter(i)

    fun handleExistingElementMove(workArea: LinearLayout, element: View, x: Float, y: Float) =
        dropManager.handleExistingElementMove(workArea, element, x, y)

    fun createElement(
        workArea: LinearLayout,
        elementType: String,
        trashArea: LinearLayout,
        placementHint: TextView
    ) =
        uiInteractionManager.createElement(workArea, elementType, trashArea, placementHint)

    fun deleteElementWithAnimation(workArea: LinearLayout, element: View) =
        workAreaManager.deleteElementWithAnimation(workArea, element)

    fun addHintView(workArea: LinearLayout) = workAreaManager.addHintView(workArea)
    fun clearHighlights(workArea: LinearLayout) = dropManager.clearHighlights(workArea)
    fun findDropTarget(workArea: LinearLayout, y: Float, placementHint: TextView) =
        dropManager.findDropTarget(workArea, y, placementHint)

    fun addElementToWorkArea(workArea: LinearLayout, element: View, x: Float, y: Float) =
        workAreaManager.addElementToWorkArea(workArea, element, x, y)

    fun addOrUpdateElementAction(id: Int, action: ElementAction) =
        eventActionManager.addOrUpdateElementAction(id, action)

    fun dpToPx(dp: Int) = UiUtils.dpToPx(context, dp)
}