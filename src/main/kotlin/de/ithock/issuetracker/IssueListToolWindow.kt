package de.ithock.issuetracker

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.TableView
import de.ithock.issuetracker.data.Issue
import de.ithock.issuetracker.data.IssueState
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GitHub
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableRowSorter

class IssueListToolWindow : ToolWindowFactory {
    private lateinit var sorter: TableRowSorter<IssueListTableModel>

    private val gitHubIssues: List<Issue>
        get() {
            // Use the GitHub API to retrieve the list of issues for the current repository
            val github = GitHub.connectUsingOAuth(GITHUB_OAUTH_TOKEN)
            val repo = github.getRepository(GITHUB_REPO)
            val issues = repo.getIssues(GHIssueState.ALL)
            val issueList: MutableList<Issue> = ArrayList()
            for (issue in issues) {
                issueList.add(Issue(issue))
            }
            return issueList
        }

    private val GITHUB_OAUTH_TOKEN: String = "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"
    private val GITHUB_REPO: String = "IT-Hock/ecc-sdk"

    private var table: TableView<Issue>? = null
    private var tableModel: IssueListTableModel? = null
    private var contextMenu: JPopupMenu? = null
    private var closeItem: JMenuItem? = null
    private var openInBrowserItem: JMenuItem? = null
    private var openItem: JMenuItem? = null
    private var commentItem: JMenuItem? = null
    private var labelItem: JMenuItem? = null
    private var editItem: JMenuItem? = null
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Use the GitHub API to retrieve the list of labels for the current repository
        val github = GitHub.connectUsingOAuth(GITHUB_OAUTH_TOKEN)
        val repo = github.getRepository(GITHUB_REPO)
        val labels = repo.listLabels().withPageSize(100).toList()

        // Create a table with the list of issues and a right-click context menu
        tableModel = IssueListTableModel()
        tableModel!!.items = gitHubIssues
        table = TableView(tableModel)
        table!!.setShowGrid(false)
        table!!.showVerticalLines = false

        // Set the table's cell renderer to use a custom renderer that will render the issue title
        // with a strikethrough if the issue is closed
        table!!.setDefaultRenderer(Issue::class.java, IssueTableCellRenderer())
        table!!.autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        table!!.setSelectionMode(/* selectionMode = */ ListSelectionModel.SINGLE_SELECTION)
        table!!.autoCreateRowSorter = true
        table!!.fillsViewportHeight = true
        table!!.rowHeight = 25

        sorter = TableRowSorter(tableModel)
        table!!.rowSorter = sorter

        // Create a right-click context menu
        contextMenu = JPopupMenu()
        openInBrowserItem = JMenuItem("Open in Browser")
        closeItem = JMenuItem("Close")
        openItem = JMenuItem("Open")
        commentItem = JMenuItem("Comment")
        labelItem = JMenuItem("Label")
        editItem = JMenuItem("Edit")
        contextMenu!!.add(closeItem)
        contextMenu!!.add(openItem)
        contextMenu!!.add(commentItem)
        contextMenu!!.add(labelItem)
        contextMenu!!.add(editItem)

        // Add a listener to the context menu items to handle the user's selection
        closeItem!!.addActionListener { e: ActionEvent? -> closeIssue() }
        openItem!!.addActionListener { e: ActionEvent? -> openIssue() }
        commentItem!!.addActionListener { e: ActionEvent? -> commentIssue() }
        labelItem!!.addActionListener { e: ActionEvent? -> labelIssue() }
        editItem!!.addActionListener { e: ActionEvent? -> editIssue() }


        // Add a mouse listener to the table to show the context menu when the user right-clicks
        // on a row
        table!!.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    showContextMenu(e)
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    showContextMenu(e)
                }
            }
        })

        // Add a mouse listener to the table to open the issue in the browser when the user double-clicks
        // on a row
        table!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val row = table!!.rowAtPoint(e.point)
                    val issue = table!!.getRow(row)
                    if (issue != null) {
                        BrowserUtil.browse(issue.getUrl())
                    }
                }
            }
        })
        val scrollPane = JBScrollPane(table)
        val panel = JPanel(BorderLayout())

        // Create layout for the filters
        val filterPanel = JPanel()
        val filterLabel = JLabel("Filter:")

        // Add custom full text search
        val searchField = JTextField()
        // On pressing enter, filter the table
        searchField.addActionListener { e: ActionEvent? ->
            val filter = searchField.text

            val rowFilter = IssueListRowFilter(labels)
            rowFilter.setFilterText(filter)
            sorter.rowFilter = rowFilter
        }

        // Add filter by status
        val statusFilter = ComboBox<String>()
        statusFilter.addItem("All")
        statusFilter.addItem("Open")
        statusFilter.addItem("Closed")
        // TODO: Do this asynchronously
        statusFilter.addActionListener {
            // Append the status filter to the full text search
            val status = statusFilter.selectedItem as String
            val search = searchField.text
            val filter = if (status == "All") {
                // Use regex to remove the status filter from the full text search
                search.replace("status:open|status:closed|is:open|is:closed", "")
            } else "$search status:$status"

            val rowFilter = IssueListRowFilter(labels)
            rowFilter.setFilterText(filter)
            sorter.rowFilter = rowFilter

            searchField.text = filter
        }

        // Add filter by label
        val labelFilter = ComboBox<IssueLabel>()
        labelFilter.addItem(IssueLabel(-1, "All"))
        for (label in labels) {
            labelFilter.addItem(IssueLabel(label.id, label.name))
        }

        labelFilter.addActionListener {
            // Append the filter by label to the full text search
            val label = labelFilter.selectedItem as IssueLabel
            val search = searchField.text
            val filter = if (label.id == -1L) {
                // Use regex to remove the label filter from the full text search
                search.replace("label:.*", "")
            } else "$search label:${label.id}"

            val rowFilter = IssueListRowFilter(labels)
            rowFilter.setFilterText(filter)
            sorter.rowFilter = rowFilter

            searchField.text = filter
        }

        // Add to filter panel
        filterPanel.add(filterLabel)
        filterPanel.add(searchField)
        filterPanel.add(statusFilter)
        filterPanel.add(labelFilter)

        // Add to main panel
        panel.add(filterPanel, BorderLayout.NORTH)

        panel.add(scrollPane, BorderLayout.CENTER)
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content = contentFactory.createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    inner class IssueLabel(val id: Long, private val text: String) {
        override fun toString(): String {
            return text
        }
    }

    private fun editIssue() {
        TODO("Not yet implemented")
    }

    private fun labelIssue() {
        TODO("Not yet implemented")
    }

    private fun commentIssue() {
        TODO("Not yet implemented")
    }

    private fun openIssue() {
        TODO("Not yet implemented")
    }

    private fun closeIssue() {
        TODO("Not yet implemented")
    }

    private fun showContextMenu(e: MouseEvent) {
        // Show the context menu when the user right-clicks on a row
        val row = table!!.rowAtPoint(e.point)
        if (row >= 0) {
            table!!.setRowSelectionInterval(row, row)
            val issue = table!!.getRow(row)
            if (issue != null) {
                closeItem!!.isEnabled = issue.getState() != IssueState.CLOSED
                openItem!!.isEnabled = issue.getState() == IssueState.CLOSED
                commentItem!!.isEnabled = true
                labelItem!!.isEnabled = true
                editItem!!.isEnabled = true
                contextMenu!!.show(e.component, e.x, e.y)
            }
        }
    }

    private inner class IssueTableCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            // Customize the appearance of the table cells based on the value and state of the issue
            val c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            val state = tableModel!!.getValueAt(row, 2) as String
            if (state == "open") {
                c.foreground = JBColor.GREEN
            } else {
                c.foreground = JBColor.RED
            }
            return c
        }
    }
}