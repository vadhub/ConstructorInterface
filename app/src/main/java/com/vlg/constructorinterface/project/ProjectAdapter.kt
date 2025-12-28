package com.vlg.constructorinterface.project

import android.app.AlertDialog
import android.content.Context
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.vlg.constructorinterface.R

class ProjectAdapter(
    private val projects: MutableList<Project>,
    private val onProjectClick: (Project) -> Unit,
    private val onProjectDelete: (Project) -> Unit,
    private val onProjectEdit: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProjectName: TextView = view.findViewById(R.id.tvProjectName)
        val tvProjectPath: TextView = view.findViewById(R.id.tvProjectPath)
        val menuButton: ImageButton = view.findViewById(R.id.buttonMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]

        holder.tvProjectName.text = project.name
        holder.tvProjectPath.text = project.path

        holder.itemView.setOnClickListener {
            onProjectClick(project)
        }

        // Настройка меню для каждой записи
        holder.menuButton.setOnClickListener { v ->
            showProjectMenu(v, project)
        }
    }

    override fun getItemCount() = projects.size

    private fun showProjectMenu(view: View, project: Project) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.menu_project_item, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    onProjectEdit(project)
                    true
                }
                R.id.menu_delete -> {
                    showDeleteConfirmation(view.context, project)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showDeleteConfirmation(context: Context, project: Project) {
        AlertDialog.Builder(context)
            .setTitle("Удалить проект")
            .setMessage("Вы уверены, что хотите удалить проект \"${project.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                onProjectDelete(project)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    fun updateProject(updatedProject: Project) {
        val position = projects.indexOfFirst { it.id == updatedProject.id }
        if (position != -1) {
            projects[position] = updatedProject
            notifyItemChanged(position)
        }
    }
}