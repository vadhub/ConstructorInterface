package com.vlg.constructorinterface.project

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vlg.constructorinterface.R
import java.io.File

class ProjectSelectionFragment : Fragment() {

    private lateinit var recyclerViewProjects: RecyclerView
    private lateinit var emptyStateView: LinearLayout
    private lateinit var textEmptyMessage: TextView
    private lateinit var buttonCreateNew: Button
    private lateinit var fabAddProject: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private lateinit var projectAdapter: ProjectAdapter
    private val projectList = mutableListOf<Project>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_project_selection, container, false)

        // Инициализация view элементов
        recyclerViewProjects = view.findViewById(R.id.recyclerViewProjects)
        emptyStateView = view.findViewById(R.id.emptyStateView)
        textEmptyMessage = view.findViewById(R.id.textEmptyMessage)
        buttonCreateNew = view.findViewById(R.id.buttonCreateNew)
        fabAddProject = view.findViewById(R.id.fabAddProject)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("projects", 0)

        setupRecyclerView()
        loadProjects()
        setupListeners()
        updateUI()
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter(
            projectList,
            onProjectClick = { project ->
                onProjectSelected(project)
            },
            onProjectDelete = { project ->
                deleteProject(project)
            },
            onProjectEdit = { project ->
                showEditProjectDialog(project)
            }
        )

        recyclerViewProjects.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewProjects.adapter = projectAdapter

        // Добавляем разделитель между элементами
        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        recyclerViewProjects.addItemDecoration(dividerItemDecoration)
    }

    private fun loadProjects() {
        progressBar.visibility = View.VISIBLE

        // Имитация загрузки (можно удалить в реальном приложении)
        recyclerViewProjects.postDelayed({
            val json = sharedPreferences.getString("project_list", null)
            projectList.clear()

            if (json != null) {
                val type = object : TypeToken<List<Project>>() {}.type
                val loadedList: List<Project> = gson.fromJson(json, type) ?: emptyList()
                projectList.addAll(loadedList)
            }

            progressBar.visibility = View.GONE
            updateUI()
        }, 300)
    }

    private fun saveProjects() {
        val json = gson.toJson(projectList)
        sharedPreferences.edit {
            putString("project_list", json)
        }
    }

    private fun setupListeners() {
        fabAddProject.setOnClickListener {
            showCreateProjectDialog()
        }

        buttonCreateNew.setOnClickListener {
            showCreateProjectDialog()
        }
    }

    private fun updateUI() {
        if (projectList.isEmpty()) {
            recyclerViewProjects.visibility = View.GONE
            emptyStateView.visibility = View.VISIBLE
            fabAddProject.visibility = View.GONE
            textEmptyMessage.text = "Нет проектов. Создайте свой первый проект."
        } else {
            recyclerViewProjects.visibility = View.VISIBLE
            emptyStateView.visibility = View.GONE
            fabAddProject.visibility = View.VISIBLE
            projectAdapter.notifyDataSetChanged()
        }
    }

    private fun showCreateProjectDialog() {
        val dialog = ProjectDialogFragment().apply {
            setOnProjectCreatedListener(object : ProjectDialogFragment.OnProjectCreatedListener {
                override fun onProjectCreated(project: Project) {
                    addProject(project)
                }
            })
        }
        dialog.show(parentFragmentManager, "create_project_dialog")
    }

    private fun showEditProjectDialog(project: Project) {
        val dialog = ProjectDialogFragment.newInstance(project).apply {
            setOnProjectCreatedListener(object : ProjectDialogFragment.OnProjectCreatedListener {
                override fun onProjectCreated(project: Project) {
                    updateProject(project)
                }
            })
        }

        dialog.show(parentFragmentManager, "edit_project_dialog")
    }

    private fun addProject(project: Project) {
        projectList.add(project)
        saveProjects()
        projectAdapter.notifyItemInserted(projectList.size - 1)
        updateUI()
    }

    private fun updateProject(updatedProject: Project) {
        val position = projectList.indexOfFirst { it.id == updatedProject.id }
        if (position != -1) {
            projectList[position] = updatedProject
            saveProjects()
            projectAdapter.updateProject(updatedProject)
        }
    }

    private fun deleteProject(project: Project) {
         val projectDir = File(project.path)
         projectDir.deleteRecursively()

        // Удаляем из списка
        val position = projectList.indexOfFirst { it.id == project.id }
        if (position != -1) {
            projectList.removeAt(position)
            saveProjects()
            projectAdapter.notifyItemRemoved(position)
            updateUI()
        }
    }

    private fun onProjectSelected(project: Project) {
        sharedPreferences.edit {
            putString("current_project", gson.toJson(project))
        }

        // Сообщаем Activity о выборе проекта
        val listener = activity as? OnProjectSelectedListener
        listener?.onProjectSelected(project)
    }

    interface OnProjectSelectedListener {
        fun onProjectSelected(project: Project)
    }
}