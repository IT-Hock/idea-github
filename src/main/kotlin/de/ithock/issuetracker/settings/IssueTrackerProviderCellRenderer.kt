package de.ithock.issuetracker.settings

import java.awt.Component
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

/* Provider Cell Renderer */
class IssueTrackerProviderCellRenderer : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        if (value is IssueTrackerType) {
            text = value.name
            icon = value.icon
        }
        return component
    }
}