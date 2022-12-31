package de.ithock.advancedissuetracker.codeInsight

import com.intellij.openapi.editor.markup.GutterIconRenderer
import de.ithock.issuetracker.util.Icons
import javax.swing.Icon

class IssueGutterRenderer : GutterIconRenderer() {
    override fun getIcon(): Icon {
        return Icons.IssueOpen
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}