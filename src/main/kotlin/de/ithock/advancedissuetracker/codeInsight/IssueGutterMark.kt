package de.ithock.advancedissuetracker.codeInsight

import com.intellij.codeInsight.daemon.GutterMark
import de.ithock.issuetracker.util.Icons
import javax.swing.Icon

class IssueGutterMark :GutterMark {
    override fun getIcon(): Icon {
        return Icons.IssueOpen
    }

    override fun getTooltipText(): String {
        return "Issue"
    }

}
