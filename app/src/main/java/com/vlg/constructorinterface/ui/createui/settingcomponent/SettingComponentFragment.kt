package com.vlg.constructorinterface.ui.createui.settingcomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.vlg.constructorinterface.R
import com.vlg.constructorinterface.model.ElementEvent
import com.vlg.constructorinterface.model.info
import com.vlg.constructorinterface.ui.createui.CreatorUI

private const val COUNT_CHARS_ID = 40
private const val ARG_VIEW_TAG = "view_tag"
private const val ARG_CURRENT_TEXT = "current_text"

class SettingComponentFragment : DialogFragment() {

    private lateinit var idEditText: EditText
    private lateinit var charIDCount: TextView
    private lateinit var editText: EditText
    private lateinit var charCount: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var addEventButton: Button
    private lateinit var countEvents: TextView
    private lateinit var eventsList: LinearLayout

    private lateinit var textInputManager: TextInputManager

    private lateinit var creatorUI: CreatorUI

    private var viewTag: String? = null
    private var currentText: String? = null

    private var onSettingCompleteListener: OnSettingCompleteListener? = null

    interface OnSettingCompleteListener {
        fun onSettingsSaved(tag: String, newText: String, newId: String)
        fun onSettingsCancelled()
    }

    fun setOnSettingCompleteListener(listener: OnSettingCompleteListener) {
        this.onSettingCompleteListener = listener
    }

    fun setCreatorUI(creatorUI: CreatorUI) {
        this.creatorUI = creatorUI
    }

    companion object {
        fun newInstance(viewTag: String, currentText: String): SettingComponentFragment {
            val fragment = SettingComponentFragment()
            val args = Bundle().apply {
                putString(ARG_VIEW_TAG, viewTag)
                putString(ARG_CURRENT_TEXT, currentText)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewTag = it.getString(ARG_VIEW_TAG)
            currentText = it.getString(ARG_CURRENT_TEXT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting_component, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initViews(view)
        setupButtons()

        textInputManager = TextInputManager()

        updateEventInfo()

        textInputManager.setupTextWatchers(editText, charCount, currentText ?: "")
        textInputManager.setupTextWatchers(idEditText, charIDCount, viewTag ?: "")
        charCount.text = textInputManager.getCurrentCharCountText(currentText ?: "")
        charIDCount.text = textInputManager.getCurrentCharCountText(viewTag ?: "", COUNT_CHARS_ID)
    }

    private fun initViews(view: View) {
        idEditText = view.findViewById(R.id.renameIDEditText)
        charIDCount = view.findViewById(R.id.charIDCount)
        editText = view.findViewById(R.id.renameEditText)
        charCount = view.findViewById(R.id.charCount)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        addEventButton = view.findViewById(R.id.addEventButton)
        countEvents = view.findViewById(R.id.totalCountEvents)
        eventsList = view.findViewById(R.id.eventsList)
    }

    private fun setupButtons() {
        saveButton.setOnClickListener {
            handleSave()
            dismiss()
        }

        addEventButton.setOnClickListener {
            showEventDialog(object : EventDialog.OnCompleteListener{
                override fun onSaved() {
                    updateEventInfo()
                }

                override fun onCancelled() {}
            })
        }

        cancelButton.setOnClickListener {
            onSettingCompleteListener?.onSettingsCancelled()
            dismiss()
        }
    }

    private fun updateEventInfo() {
        addEventsToLayout(creatorUI.getEventsByTag(viewTag ?: ""), eventsList)
        countEvents.text = "Событий на элементе: " + creatorUI.getCountOfEventsByTag(viewTag ?: "")
    }

    private fun showEventDialog(onCompleteListener: EventDialog.OnCompleteListener) {
        val eventDialog = EventDialog(viewTag ?: "", creatorUI)
        eventDialog.setOnSettingCompleteListener(onCompleteListener)
        eventDialog.show(childFragmentManager, "EventDialog")

    }

    private fun handleSave() {
        val newText = editText.text.toString().trim()
        val newId = idEditText.text.toString().trim()

        val oldTag = viewTag ?: ""

        if (!textInputManager.isValidTag(editText.context,newId, COUNT_CHARS_ID)) return

        onSettingCompleteListener?.onSettingsSaved(oldTag, newText, newId)
        parentFragmentManager.popBackStack()
    }

    fun addEventsToLayout(
        events: List<ElementEvent>,
        linearLayout: LinearLayout
    ) {
        linearLayout.removeAllViews()
        if (events.isEmpty()) {
            linearLayout.visibility = View.GONE
            return
        }

        linearLayout.visibility = View.VISIBLE

        val inflater = LayoutInflater.from(linearLayout.context)

        for (event in events) {
            val view = inflater.inflate(R.layout.item_event, linearLayout, false)

            val tvProjectName = view.findViewById<TextView>(R.id.tvProjectName)
            val buttonMenu = view.findViewById<ImageButton>(R.id.buttonMenu)

            tvProjectName.text = event.info()

            buttonMenu.setOnClickListener { showMenu(linearLayout) }

            linearLayout.addView(view)
        }
    }

    private fun showMenu(view: View) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.menu_event, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {

                    true
                }
                R.id.menu_delete -> {

                    true
                }
                else -> false
            }
        }
        popup.show()
    }

}