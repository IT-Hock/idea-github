package de.ithock.issuetracker.toolwindows

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.AnActionButton
import com.intellij.ui.JBColor
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.content.ContentFactory
import com.intellij.uiDesigner.core.GridConstraints
import de.ithock.issuetracker.PluginBundle
import de.ithock.issuetracker.ui.IssueListPanel
import org.kohsuke.github.GHEventPayload.Issue
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel

class AdvancedIssueListToolWindow : ToolWindowFactory {
    init {

    }
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = JPanel(

        ).apply {
            layout = GridLayout(1, 2)
            add(IssueListPanel(project))
            /*
            // Add toolbar to panel
            ActionToolbarImpl(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, object : ActionGroup() {
                override fun getChildren(e: AnActionEvent?): Array<AnAction> {
                    return arrayOf(
                        RefreshAction(),
                        SettingsAction(),
                    )
                }
            }, false).also {
                // Border is needed
                it.border = BorderFactory.createLineBorder(JBColor.border(), 1)
                it.targetComponent = this
                it.maximumSize = Dimension(28, 0)
                add(it.component,
                    GridConstraints(
                        0,
                        0,
                        1,
                        1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_VERTICAL,
                        GridConstraints.SIZEPOLICY_FIXED,
                        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                        Dimension(28, 0),
                        Dimension(28, 0),
                        Dimension(28, 0),
                        0,
                        false
                    )
                )
            }
            add(JLabel("Hello World"),
                GridConstraints(
                    0,
                    1,
                    1,
                    1,
                    GridConstraints.ANCHOR_CENTER,
                    GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                    null,
                    null,
                    null,
                    0,
                    false
                )
                )*/
        }

        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(panel, PluginBundle.get("toolwindow.title"), false)

        toolWindow.contentManager.addContent(content)
    }

    private fun createUIComponents(toolWindow: ToolWindow, panel: JPanel) {
        // Toolbar on the right of the tool window with refresh, settings, etc.
        val toolbarDecorator = ToolbarDecorator.createDecorator(toolWindow.component)
        toolbarDecorator.addExtraAction(RefreshAction())
        toolbarDecorator.addExtraAction(SettingsAction())
        panel.add(toolbarDecorator.createPanel())
    }

    class SettingsAction : AnAction() {
        init {
            templatePresentation.text = PluginBundle.get("toolwindow.toolbar.settings")
            templatePresentation.description = PluginBundle.get("toolwindow.toolbar.settings.tooltip")
            templatePresentation.icon = AllIcons.General.Settings
        }

        override fun actionPerformed(e: AnActionEvent) {
            TODO("Not yet implemented")
        }
    }

    class RefreshAction : AnAction() {

        init {
            templatePresentation.text = PluginBundle.get("toolwindow.toolbar.refresh")
            templatePresentation.description = PluginBundle.get("toolwindow.toolbar.refresh.tooltip")
            templatePresentation.icon = AllIcons.Actions.Refresh
        }

        override fun actionPerformed(e: AnActionEvent) {
            TODO("Not yet implemented")
        }
    }
}