package de.ithock.advancedissuetracker.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import de.ithock.issuetracker.PluginBundle
import java.awt.Component


class GoToIssuePopupAction : AnAction(
    PluginBundle.get("actions.goto_issue"),
    PluginBundle.get("actions.goto_issue.description"),
    AllIcons.Actions.Find
    ), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return

        /*val issuesPanel: JiraIssuesPanel = e.getRequiredData<Any>(JiraUiDataKeys.ISSUES_PANEL) as JiraIssuesPanel
        val issueKeys = issuesPanel.getJiraIssueTable().getModel().getItems().stream().map(JiraIssue::getKey)
            .collect(Collectors.toList()) as List<String>
        val popup = GoToIssuePopup(project, issueKeys) { key -> issuesPanel.goToIssue(key) }
        popup.show(issuesPanel.getJiraIssueTable() as Component)*/
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun update(event: AnActionEvent) {
        val project: Project? = event.project
        if(project == null || !project.isInitialized || project.isDisposed) {
            event.presentation.isEnabledAndVisible = false
            return
        }

        /*val manager: JiraServerManager =
            ApplicationManager.getApplication().getService(JiraServerManager::class.java) as JiraServerManager
        event.presentation.isEnabled = manager.hasJiraServerConfigured(project)*/
    }
}
