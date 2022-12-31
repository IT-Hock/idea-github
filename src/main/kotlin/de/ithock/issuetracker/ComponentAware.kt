package de.ithock.issuetracker

import com.intellij.openapi.project.Project

interface ComponentAware {
    fun getProject(): Project

    companion object {
        fun <T : ComponentAware> getComponent(project: Project, componentClass: Class<T>): T {
            return project.getComponent(componentClass)
        }
    }
}