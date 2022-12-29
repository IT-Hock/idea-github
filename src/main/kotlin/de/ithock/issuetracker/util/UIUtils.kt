package de.ithock.issuetracker.util

import com.intellij.uiDesigner.core.GridConstraints
import de.ithock.issuetracker.util.UIUtils.Companion.Separator
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator

class UIUtils {
    companion object {
        fun JPanel.Separator(column: Int = 0, columnSpan: Int = 1, rowSpan: Int = 1, maxSize:Dimension = Dimension(0, 0)) {
            this.add(
                JSeparator(
                    JSeparator.HORIZONTAL
                ), GridConstraints(
                    this.componentCount,
                    column,
                    rowSpan,
                    columnSpan,
                    GridConstraints.ANCHOR_CENTER,
                    GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null,
                    null,
                    maxSize.takeIf { it.width > 0 || it.height > 0 },
                    0,
                    false
                )
            )
        }

        fun JPanel.Label(text: String, column: Int = 0, columnSpan: Int = 1, rowSpan: Int = 1) : JLabel {
            val label = JLabel(text)
            this.add(
                label,
                GridConstraints(
                    this.componentCount,
                    column,
                    rowSpan,
                    columnSpan,
                    GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null,
                    null,
                    null,
                    0,
                    false
                )
            )
            return label
        }

        fun JPanel.CenteredLabel(text: String, column: Int = 0, columnSpan: Int = 1, rowSpan: Int = 1): JLabel {
            val label = JLabel(text)
            label.horizontalAlignment = JLabel.CENTER
            this.add(
                label,
                GridConstraints(
                    this.componentCount,
                    column,
                    rowSpan,
                    columnSpan,
                    GridConstraints.ANCHOR_CENTER,
                    GridConstraints.FILL_HORIZONTAL,
                    GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED,
                    null,
                    null,
                    null,
                    0,
                    false
                )
            )
            return label
        }
    }
}