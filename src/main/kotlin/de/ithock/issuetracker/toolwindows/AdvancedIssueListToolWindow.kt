package de.ithock.issuetracker.toolwindows

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.JPanel

class AdvancedIssueListToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel()

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, "Advanced Issue List", false)
        toolWindow.contentManager.addContent(content)
    }

    
}