package de.ithock.issuetracker.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class AdvancedIssueTrackerConfigurable : Configurable {
    override fun isModified(): Boolean {
        return false
    }

    override fun getDisplayName(): String {
        return "Advanced Issue Tracker"
    }

    override fun apply() {
    }

    override fun createComponent(): AdvancedIssueTrackerSettingsPanel {
        return AdvancedIssueTrackerSettingsPanel()
    }

    override fun reset() {
    }

    override fun disposeUIResources() {
    }
}

