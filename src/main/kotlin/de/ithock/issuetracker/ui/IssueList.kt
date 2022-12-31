package de.ithock.issuetracker.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.psi.codeStyle.extractor.ui.ExtractedSettingsDialog.CellRenderer
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.IconUtil
import com.intellij.util.ui.AbstractTableCellEditor
import com.intellij.util.ui.JBFont
import de.ithock.issuetracker.PluginBundle
import de.ithock.issuetracker.data.Issue
import de.ithock.issuetracker.data.IssueLabel
import de.ithock.issuetracker.data.IssueState
import de.ithock.issuetracker.util.FakeIssueDataProvider
import de.ithock.issuetracker.util.Helpers
import java.awt.*
import java.net.URL
import java.util.*
import java.util.Timer
import javax.swing.*
import javax.swing.event.TableModelListener
import javax.swing.table.*

class IssueListPanel(
    project: Project
) : JBLoadingPanel(BorderLayout(), project) {

    private val issueTable: IssueTable = IssueTable()

    init {
        val issueListScrollPane = JBScrollPane(issueTable, 20, 31)
        add(issueListScrollPane, BorderLayout.CENTER)
        initIssueListModel()
    }

    private fun initIssueListModel() {
        issueTable.emptyText.clear()
        startLoading()
        // Fake loading
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val issues = FakeIssueDataProvider.getFakeIssues(100)
                (issueTable.model as IssueTableModel).updateIssues(issues)
                stopLoading()
            }
        }, 10000)
        //if (getIssueStoreComponent().get(this.repo).getAllIssues().isEmpty()) {
        //    getIssueStoreComponent().get(this.repo).update(this.repo).doWhenDone(this::`initIssueListModel$lambda` - 0)
        //} else {
        //
    // stopLoading()
        //}
        //getIssueUpdaterComponent().subscribe(`IssueList$initIssueListModel$2`())

    }
}

class IssueTable : JBTable() {
    init {
        TableSpeedSearch(this)

        model = IssueTableModel()
        autoResizeMode = AUTO_RESIZE_ALL_COLUMNS

        setDefaultRenderer(IssueColumnImpl::class.java) { table, value, isSelected, hasFocus, row, column ->
            if (value is IssueColumnImpl) {
                table.getDefaultRenderer(String::class.java)
                    .getTableCellRendererComponent(table, value.getValue(), isSelected, hasFocus, row, column)
            } else {
                JLabel()
            }
        }

        setDefaultRenderer(IssueState::class.java) { table, value, isSelected, hasFocus, _, _ ->
            val issueState = value as IssueState
            val panel = JPanel()
            panel.layout = BorderLayout()

            val label = JLabel()
            label.icon = issueState.getIcon()
            label.text = issueState.getName()
            if (isSelected) {
                panel.background = table.selectionBackground
                panel.foreground = table.selectionForeground
                label.foreground = table.selectionForeground
                label.icon = IconUtil.colorize(label.icon, table.selectionForeground)
            } else if (hasFocus) {
                panel.background = table.background
                panel.foreground = table.foreground
                label.foreground = table.foreground
                label.icon = IconUtil.colorize(label.icon, table.foreground)
            } else {
                panel.background = issueState.getBackgroundColor()
                panel.foreground = issueState.getForegroundColor()
                label.foreground = issueState.getForegroundColor()
                label.icon = IconUtil.colorize(label.icon, issueState.getForegroundColor())
            }
            panel.add(label)
            panel
        }
    }

    override fun getDefaultEditor(columnClass: Class<*>?): TableCellEditor {
        if (model !is IssueTableModel) {
            return super.getDefaultEditor(columnClass)
        }

        val row = selectedRow
        val column = selectedColumn
        if (row < 0 || column < 0) {
            return super.getDefaultEditor(columnClass)
        }

        val issue = (model as IssueTableModel).getIssue(row)

        if (columnClass == IssueState::class.java) {
            return IssueStateCellEditor(issue)
        }
        if (columnClass == TitleIssueColumn::class.java) {
            return IssueTitleCellEditor(issue)
        }

        return super.getDefaultEditor(columnClass)
    }

    class IssueTitleCellEditor(val issue: Issue) : AbstractTableCellEditor() {
        private val textField = JBTextField()

        init {
            textField.font = JBFont.create(textField.font)
        }

        override fun getTableCellEditorComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int
        ): Component {
            textField.text = value as String
            return textField
        }

        override fun getCellEditorValue(): Any {
            return textField.text
        }
    }

    class IssueStateCellEditor(val issue: Issue) : AbstractCellEditor(), TableCellEditor {
        private val comboBox = ComboBox(
            IssueState.values()
        )

        override fun getTableCellEditorComponent(
            table: JTable?,
            value: Any?,
            isSelected: Boolean,
            row: Int,
            column: Int
        ): Component {
            comboBox.selectedItem = value
            return comboBox
        }

        override fun getCellEditorValue(): Any? {
            return comboBox.selectedItem
        }
    }
}

enum class IssueColumn(val title: String) {
    ID(PluginBundle.get("column.id")),
    TITLE(PluginBundle.get("column.title")),
    STATE(PluginBundle.get("column.state")),
    AUTHOR(PluginBundle.get("column.author")),
    ASSIGNEE(PluginBundle.get("column.assignee")),
    CREATED_AT(PluginBundle.get("column.createdAt")),
    CLOSED_AT(PluginBundle.get("column.closedAt")),
    MILESTONE(PluginBundle.get("column.milestone"));

    fun getFormattedValue(issue: Issue): Any {
        return when (this) {
            ID -> issue.getIdentifier()
            TITLE -> issue.getTitle()
            STATE -> issue.getState()
            AUTHOR -> issue.getAuthor()
            ASSIGNEE -> issue.getAssignee() ?: ""
            CREATED_AT -> Helpers.asAgo(issue.getCreatedAt())
            CLOSED_AT -> issue.getClosedAt()?.let { Helpers.asAgo(it) } ?: ""
            MILESTONE -> issue.getMilestone() ?: ""
        }
    }
}

/* Use a wrapper class */
open class IssueColumnImpl(
    private val issueColumn: IssueColumn,
    private val value: String
) {
    fun getIssueColumn(): IssueColumn {
        return issueColumn
    }

    fun getValue(): Any {
        return value
    }

    override fun toString(): String {
        return value
    }
}

class TitleIssueColumn(
    value: String
) : IssueColumnImpl(IssueColumn.TITLE, value)

class IssueTableModel : TableModel {
    private val issues: ArrayList<Issue> = ArrayList()
    private val issueColumns: Array<IssueColumn> = IssueColumn.values()

    override fun getRowCount(): Int {
        return issues.size
    }

    override fun getColumnCount(): Int {
        return issueColumns.size
    }

    override fun getColumnName(column: Int): String {
        return issueColumns[column].title
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (issueColumns[columnIndex]) {
            IssueColumn.STATE -> IssueState::class.java
            else -> String::class.java
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return when (issueColumns[columnIndex]) {
            IssueColumn.STATE -> issues[rowIndex].getState()
            IssueColumn.TITLE -> TitleIssueColumn(issues[rowIndex].getTitle())
            else -> issueColumns[columnIndex].getFormattedValue(issues[rowIndex])
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        // Not needed
    }

    override fun addTableModelListener(l: TableModelListener?) {
        // Not needed
    }

    override fun removeTableModelListener(l: TableModelListener?) {
        // Not needed
    }

    fun getIssue(rowIndex: Int): Issue {
        return issues[rowIndex]
    }

    fun updateIssues(issues: List<Issue>) {
        this.issues.clear()
        this.issues.addAll(issues)
    }
}

