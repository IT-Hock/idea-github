package de.ithock.issuetracker.data

import com.intellij.icons.AllIcons
import com.intellij.ui.JBColor
import de.ithock.issuetracker.util.Icons
import java.awt.Color
import javax.swing.Icon

enum class IssueState(
    private val displayName: String,
    private val icon: Icon,
    private val backgroundColor: Color,
    private val foregroundColor: Color
) {
    OPEN("Open", Icons.IssueOpen, JBColor.GREEN, JBColor.BLACK),
    CLOSED("Closed", Icons.IssueClosed, JBColor.RED, JBColor.BLACK);

    fun getIcon(): Icon {
        return icon
    }

    fun getName(): String {
        return displayName
    }

    fun getBackgroundColor(): Color {
        return backgroundColor
    }

    fun getForegroundColor(): Color {
        return foregroundColor
    }

    override fun toString(): String {
        return displayName
    }
}