package de.ithock.issuetracker.settings

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ComponentPredicate
import de.ithock.issuetracker.util.SpaceApi
import java.awt.*
import java.awt.event.ActionEvent
import java.net.URL
import javax.swing.*

@Suppress("UnstableApiUsage")
class SpaceAccountDialog : DialogWrapper(true) {
    private lateinit var urlTextField: JBTextField
    private lateinit var permanentTokenTextField: JBTextField
    private lateinit var clientSecretTextField: JBTextField
    private lateinit var clientIdTextField: JBTextField
    private lateinit var authTypeComboBox: ComboBox<AuthenticationType>
    private lateinit var testConnectionButton: JButton

    private val spaceAccountModel: SpaceAccount

    fun getSpaceAccountModel(): SpaceAccount {
        return spaceAccountModel
    }

    init {
        title = "Add Space Account"

        spaceAccountModel = SpaceAccount()

        setOKButtonText("Save")
        setCancelButtonText("Cancel")

        init()
    }

    override fun createButtonsPanel(buttons: MutableList<out JButton>): JPanel {
        // Create a third action button
        testConnectionButton = createJButtonForAction(
            object : AbstractAction("Test Connection") {
                override fun actionPerformed(e: ActionEvent?) {
                    // Disable UI elements
                    urlTextField.isEnabled = false
                    permanentTokenTextField.isEnabled = false
                    clientSecretTextField.isEnabled = false
                    clientIdTextField.isEnabled = false
                    authTypeComboBox.isEnabled = false
                    testConnectionButton.isEnabled = false
                    isOKActionEnabled = false

                    SpaceApi.run { checkConnection(urlTextField.text) }.onSuccess {
                        SwingUtilities.invokeLater {
                            spaceAccountModel.connectionValidated = true
                            validate()
                            Messages.showInfoMessage(
                                "Connection to Space API was successful",
                                "Connection Test"
                            )
                        }
                    }.onError {
                        SwingUtilities.invokeLater {
                            spaceAccountModel.connectionValidated = false
                            validate()
                            Messages.showErrorDialog(
                                "The connection to the Space API couldn't be established. Please check your settings.",
                                "Connection to Space failed"
                            )
                        }
                    }.onProcessed {
                        SwingUtilities.invokeLater {
                            // Enable UI elements
                            urlTextField.isEnabled = true
                            permanentTokenTextField.isEnabled = true
                            clientSecretTextField.isEnabled = true
                            clientIdTextField.isEnabled = true
                            authTypeComboBox.isEnabled = true
                            testConnectionButton.isEnabled = true
                            isOKActionEnabled = true
                        }
                    }
                }
            },
        )
        val panel = super.createButtonsPanel(buttons)
        panel.add(testConnectionButton, 0)
        return panel
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("URL") {
                urlTextField = textField().bindText(spaceAccountModel::url).component
            }
            row("Authentication Type") {
                authTypeComboBox = comboBox(
                    arrayOf(
                        AuthenticationType.CLIENT_CREDENTIALS, AuthenticationType.PERMANENT_TOKEN
                    )
                ).bindItem(spaceAccountModel::authenticationType).component
            }
            row("Client ID") {
                clientIdTextField = textField().bindText(spaceAccountModel::clientId).component
            }.visibleIf(ComboBoxPredicate(authTypeComboBox) {
                it == AuthenticationType.CLIENT_CREDENTIALS
            })
            row("Client Secret") {
                clientSecretTextField = textField().bindText(spaceAccountModel::clientSecret).component
            }.visibleIf(ComboBoxPredicate(authTypeComboBox) {
                it == AuthenticationType.CLIENT_CREDENTIALS
            })
            row("Permanent Token") {
                permanentTokenTextField = textField().bindText(spaceAccountModel::permanentToken).component
            }.visibleIf(ComboBoxPredicate(authTypeComboBox) {
                it == AuthenticationType.PERMANENT_TOKEN
            })
        }

    }

    override fun doValidate(): ValidationInfo? {
        if (urlTextField.text.isNullOrBlank()) {
            return ValidationInfo("URL is required", urlTextField)
        }

        try {
            val url = URL(urlTextField.text)
            if (url.protocol != "https" && url.protocol != "http") {
                return ValidationInfo("URL must be http or https", urlTextField)
            }

            if (url.host.isBlank()) {
                return ValidationInfo("URL must have a host", urlTextField)
            }
        } catch (e: Exception) {
            return ValidationInfo("URL is invalid", urlTextField)
        }

        if (authTypeComboBox.selectedItem == AuthenticationType.CLIENT_CREDENTIALS) {
            if (clientIdTextField.text.isNullOrBlank()) {
                return ValidationInfo("Client ID is required", clientIdTextField)
            }

            if (clientSecretTextField.text.isNullOrBlank()) {
                return ValidationInfo("Client Secret is required", clientSecretTextField)
            }
        } else if (authTypeComboBox.selectedItem == AuthenticationType.PERMANENT_TOKEN) {
            // Clear validation errors of client id and client secret
            clientSecretTextField.invalidate()
            clientIdTextField.invalidate()
            if (permanentTokenTextField.text.isNullOrBlank()) {
                return ValidationInfo("Permanent Token is required", permanentTokenTextField)
            }
        }

        if(!spaceAccountModel.connectionValidated) {
            return ValidationInfo("Please check the connection to the Space API before saving")
        }
        return null
    }

    /**
     * What the f JetBrains!? Why is this not part of the DSL?
     *
     * @param T
     * @property comboBox
     * @property predicate
     * @constructor Create empty Combo box predicate
     */
    inner class ComboBoxPredicate<T>(private val comboBox: ComboBox<T>, private val predicate: (T?) -> Boolean) :
        ComponentPredicate() {
        override fun invoke(): Boolean = predicate(comboBox.selectedItem as T?)

        override fun addListener(listener: (Boolean) -> Unit) {
            comboBox.addActionListener {
                listener(predicate(comboBox.selectedItem as T?))
            }
        }
    }

    enum class AuthenticationType(val displayName: String, val value: Number) {
        CLIENT_CREDENTIALS("Client Credentials", 0), PERMANENT_TOKEN("Permanent Token", 1);

        override fun toString(): String {
            return displayName
        }
    }

    data class SpaceAccount(
        var connectionValidated: Boolean = false,
        var url: String = "",
        var clientId: String = "",
        var clientSecret: String = "",
        var permanentToken: String = "",
        var authenticationType: AuthenticationType = AuthenticationType.CLIENT_CREDENTIALS
    ){
        fun getCredentials(): Credentials {
            return when(authenticationType) {
                AuthenticationType.CLIENT_CREDENTIALS -> Credentials(clientId, clientSecret)
                AuthenticationType.PERMANENT_TOKEN -> Credentials("", permanentToken)
            }
        }
    }
}