package de.ithock.issuetracker

import com.intellij.openapi.options.ConfigurationException
import de.ithock.issuetracker.util.Settings
import org.jetbrains.annotations.Nls
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Issue tracker project configurable
 *
 *
 * @constructor Create empty Issue tracker project configurable
 */
/*class IssueTrackerProjectConfigurable : com.intellij.openapi.options.Configurable {

    private val panel = JPanel()
    private val repositoryField = JTextField()
    private val accessTokenField = JTextField()

    @Nls
    override fun getDisplayName(): String {
        return "Issue Tracker"
    }

    override fun createComponent(): JComponent {
        panel.add(repositoryField)
        panel.add(accessTokenField)
        return panel
    }

    override fun isModified(): Boolean {
        val repository = repositoryField.text
        val accessToken = accessTokenField.text
        return repository != Settings.repository || accessToken != Settings.accessToken
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        Settings.repository = repositoryField.text
        Settings.accessToken = accessTokenField.text
    }
}*/