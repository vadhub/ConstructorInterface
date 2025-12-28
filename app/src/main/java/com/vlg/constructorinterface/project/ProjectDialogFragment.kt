package com.vlg.constructorinterface.project

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vlg.constructorinterface.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectDialogFragment : DialogFragment() {

    interface OnProjectCreatedListener {
        fun onProjectCreated(project: Project)
    }

    private var listener: OnProjectCreatedListener? = null
    private var existingProject: Project? = null

    companion object {
        private const val ARG_PROJECT = "project"

        fun newInstance(project: Project? = null): ProjectDialogFragment {
            val fragment = ProjectDialogFragment()
            if (project != null) {
                val args = Bundle()
                val gson = Gson()
                args.putString(ARG_PROJECT, gson.toJson(project))
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val projectJson = it.getString(ARG_PROJECT)
            if (projectJson != null) {
                val gson = Gson()
                existingProject = gson.fromJson(projectJson, Project::class.java)
            }
        }
    }

    fun setOnProjectCreatedListener(listener: OnProjectCreatedListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_project, null)

        setupViews(view)

        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        return dialog
    }

    private fun setupViews(view: View) {
        val editTextName = view.findViewById<EditText>(R.id.editTextProjectName)
        val buttonAction = view.findViewById<Button>(R.id.buttonAction)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        // Если редактируем существующий проект
        existingProject?.let { project ->
            editTextName.setText(project.name)
            buttonAction.text = "Сохранить"
        } ?: run {
            buttonAction.text = "Создать"

            // Генерируем имя по умолчанию
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val defaultName = "Проект ${dateFormat.format(Date())}"
            editTextName.setText(defaultName)
        }

        buttonAction.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val path = System.currentTimeMillis().toString()+"cr"

            if (name.isEmpty()) {
                editTextName.error = "Введите название проекта"
                editTextName.requestFocus()
                return@setOnClickListener
            }

            // Проверяем, существует ли уже проект с таким именем (кроме текущего редактируемого)
            val duplicateExists = checkForDuplicateName(name)
            if (duplicateExists) {
                editTextName.error = "Проект с таким именем уже существует"
                editTextName.requestFocus()
                return@setOnClickListener
            }

            val project = if (existingProject != null) {
                existingProject!!.copy(
                    name = name,
                    path = path,
                    createdAt = System.currentTimeMillis()
                )
            } else {
                Project(
                    name = name,
                    path = path,
                )
            }

            listener?.onProjectCreated(project)
            dismiss()
        }

        buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun checkForDuplicateName(newName: String): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("projects", 0)
        val json = sharedPreferences.getString("project_list", null)

        if (json != null) {
            val type = object : TypeToken<List<Project>>() {}.type
            val projects: List<Project> = Gson().fromJson(json, type) ?: emptyList()

            return projects.any { it.name == newName && it.id != existingProject?.id }
        }

        return false
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}