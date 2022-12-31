package de.ithock.advancedissuetracker

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.XmlSerializerUtil
import de.ithock.advancedissuetracker.implementations.IssueTrackerConnection
import de.ithock.advancedissuetracker.implementations.Issue
import de.ithock.advancedissuetracker.util.AvatarCache

@Service(Service.Level.PROJECT)
@State(name = "AdvancedIssueTrackerProjectService", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class IssueTrackerProjectService() : PersistentStateComponent<IssueTrackerProjectService> {
    @com.intellij.util.xmlb.annotations.Transient
    var connection: IssueTrackerConnection? = null

    var issues:List<Issue> = listOf()
    var projectId:String? = null

    override fun getState(): IssueTrackerProjectService {
        return this
    }

    override fun loadState(state: IssueTrackerProjectService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(project:Project? = null): IssueTrackerProjectService {
            var projectToUse = project
            if(project == null) {
                if (ProjectManager.getInstance().openProjects.isEmpty()) {
                    throw IllegalStateException("No open projects")
                }

                projectToUse = ProjectManager.getInstance().openProjects[0]
            }
            if(projectToUse == null || projectToUse.isDisposed) {
                throw IllegalStateException("Project is disposed")
            }

            return projectToUse.getService(IssueTrackerProjectService::class.java)
        }
    }
}