package de.ithock.advancedissuetracker.codeInsight.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.rows
import com.intellij.ui.dsl.builder.text
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.applyIf
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.JButtonAction
import de.ithock.advancedissuetracker.IssueTrackerProjectService
import de.ithock.advancedissuetracker.IssueTrackerService
import de.ithock.advancedissuetracker.implementations.Issue
import de.ithock.advancedissuetracker.util.AvatarCache
import java.awt.Dimension
import java.util.*
import javax.swing.*
import kotlin.collections.ArrayList

/**
 * How it currently looks:
 * @link https://user-images.githubusercontent.com/20743379/210153378-8d31d26c-54d9-4825-bb68-c408b5ee21dd.png
 */
class IssueHover(
    private val issue: Issue,
    private val actions: List<JComponent> = emptyList()
) : JPanel() {
    private var issueDetailsPanel: JPanel
    private val issueSummaryPanel: JPanel
    private val issueAuthorPanel: JPanel
    private val issueLinksPanel: JPanel

    private val issueDetailsLabel: JLabel = JBLabel()
    private val issueIdentifierLabel: JLabel = JLabel()
    private val issueSummary: JLabel = JLabel()
    private val issueAuthorAvatar: JLabel = JLabel()
    private val issueAuthorNameLabel: JLabel = JLabel()


    init {
        layout = GridLayoutManager(4, 1, JBUI.insets(10), 0, 0)

        issueSummaryPanel = createIssueSummary()
        add(
            issueSummaryPanel, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null
            )
        )

        issueAuthorPanel = createIssueAuthorPanel()
        add(
            issueAuthorPanel, GridConstraints(
                1,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null
            )
        )

        issueDetailsPanel = createIssueDetailsPanel()
        add(
            issueDetailsPanel, GridConstraints(
                2,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )

        issueLinksPanel = createIssueLinksPanel()
        add(
            issueLinksPanel, GridConstraints(
                3,
                0,
                1,
                1,
                GridConstraints.ANCHOR_EAST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK,
                null,
                null,
                null
            )
        )
    }

    private fun createIssueLinksPanel() : JPanel {
        if (actions.isEmpty()) {
            return JPanel()
        }

        val linksPanel = JPanel()
        linksPanel.layout = GridLayoutManager(1, actions.size, JBUI.insets(0, 10), 10, -1)

        for (action in actions) {
            // Create icon button
            linksPanel.add(
                action, GridConstraints(
                    0,
                    linksPanel.componentCount,
                    1,
                    1,
                    GridConstraints.ANCHOR_CENTER,
                    GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null,
                    null,
                    null
                )
            )
        }
        return linksPanel
    }

    private fun createIssueDetailsPanel() : JPanel {
        val detailsPanel = JPanel()
        detailsPanel.layout = GridLayoutManager(1, 1, JBUI.insets(5), -1, -1)
        // TODO: Formatting?

        // TODO: Status Component?
        val scrollPane = JBScrollPane()
        val textArea = JBTextArea(issue.body, 15, 10)

        textArea.isEditable = false
        textArea.wrapStyleWord = true
        textArea.lineWrap = true

        scrollPane.setViewportView(textArea)

        detailsPanel.add(
            scrollPane, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        return detailsPanel
    }

    private fun createIssueAuthorPanel(): JPanel {
        val authorPanel = JPanel()
        authorPanel.layout = GridLayoutManager(1, 2, JBUI.emptyInsets(), 5, 0)

        // Get Avatar cache
        val issueTrackerProjectService = ApplicationManager.getApplication().getService(IssueTrackerService::class.java)
        if(issueTrackerProjectService != null) {
            val avatarCache = issueTrackerProjectService.avatarCache

            // Get Avatar

            if (issue.author.avatar != null) {
                val avatar = avatarCache.load(issue.author.avatar!!).join()
                issueAuthorAvatar.text = null
                issueAuthorAvatar.icon = avatar
                issueAuthorAvatar.maximumSize = Dimension(32, 32)
                issueAuthorAvatar.minimumSize = Dimension(32, 32)
                issueAuthorAvatar.horizontalAlignment = SwingConstants.CENTER
                authorPanel.add(
                    issueAuthorAvatar, GridConstraints(
                        0,
                        0,
                        1,
                        1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_FIXED,
                        GridConstraints.SIZEPOLICY_FIXED,
                        null,
                        null,
                        null
                    )
                )
            }
        }

        if(issue.author.profileUrl != null) {
            val authorNameAction = ActionLink(issue.author.getFullName()) {
                BrowserUtil.browse(issue.author.profileUrl!!)
            }
            authorPanel.add(
                authorNameAction, GridConstraints(
                    0,
                    1,
                    1,
                    1,
                    GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null,
                    null,
                    null
                )
            )
        }else{
            issueAuthorNameLabel.text = issue.author.getFullName()
            authorPanel.add(
                issueAuthorNameLabel, GridConstraints(
                    0,
                    1,
                    1,
                    1,
                    GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_BOTH,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null,
                    null,
                    null
                )
            )
        }
        return authorPanel
    }

    private fun createIssueSummary(): JPanel {
        val summaryPanel = JPanel()
        summaryPanel.layout = GridLayoutManager(1, 2, JBUI.emptyInsets(), 10, 0)

        issueIdentifierLabel.text = issue.identifier
        summaryPanel.add(
            issueIdentifierLabel, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )

        issueSummary.text = issue.summary
        summaryPanel.add(
            issueSummary, GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        return summaryPanel
    }


    interface ActionInvokedListener : EventListener {
        fun onActionInvoked()
    }

    private val actionInvokedListeners: MutableList<ActionInvokedListener> = ArrayList()
    fun onActionInvoked(listener: ActionInvokedListener) {
        actionInvokedListeners.add(listener)
    }

    private fun notifyActionInvokedListeners() {
        for (listener in actionInvokedListeners) {
            listener.onActionInvoked()
        }
    }
}