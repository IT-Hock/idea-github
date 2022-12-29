package de.ithock.issuetracker.settings

import com.intellij.ui.table.JBTable
import javax.swing.JLabel
import javax.swing.event.TableModelListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableModel

class AccountsTable : JBTable() {
    override fun getCellRenderer(row: Int, column: Int): TableCellRenderer {
        if (column == 0) {
            return TableCellRenderer { table, value, isSelected, hasFocus, row, column ->
                if (value !is IssueTrackerType) {
                    JLabel(value.toString())
                }
                val issueTrackerType = value as IssueTrackerType
                DefaultTableCellRenderer().getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                ).apply {
                    if(this !is JLabel)
                    {
                        return@apply
                    }
                    text = issueTrackerType.displayName
                    icon = issueTrackerType.icon
                }
            }
        }
        return super.getCellRenderer(row, column)
    }
}

class AccountsTableModel(val data: Array<IssueTrackerAccount>) : TableModel {
    // Create custom cell renderer for column 0

    override fun getRowCount(): Int {
        return data.size
    }

    override fun getColumnCount(): Int {
        return 3
    }

    override fun getColumnName(column: Int): String {
        return when (column) {
            0 -> "Provider"
            1 -> "URL"
            2 -> "Unique ID"
            else -> ""
        }
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            0 -> IssueTrackerType::class.java
            1 -> String::class.java
            2 -> String::class.java
            else -> String::class.java
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return false
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return when (columnIndex) {
            0 -> data[rowIndex].type
            1 -> data[rowIndex].url
            2 -> data[rowIndex].uniqueId
            else -> ""
        }
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
    }

    override fun addTableModelListener(l: TableModelListener?) {
    }

    override fun removeTableModelListener(l: TableModelListener?) {
    }
}