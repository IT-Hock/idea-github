package de.ithock.issuetracker.settings

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.icons.AllIcons
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import de.ithock.issuetracker.util.Icons
import de.ithock.issuetracker.util.SettingsUtil
import de.ithock.issuetracker.util.UIUtils.Companion.CenteredLabel
import de.ithock.issuetracker.util.UIUtils.Companion.Label
import de.ithock.issuetracker.util.UIUtils.Companion.Separator
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.lang.management.ManagementFactory
import java.util.*
import javax.swing.*
import kotlin.collections.ArrayList


enum class IssueTrackerType(val displayName: String, val icon: Icon) {
    SPACE("Space", Icons.Space),
    YOUTRACK("YouTrack (coming soon)", Icons.YouTrack),
    GITHUB("GitHub (coming soon)", AllIcons.Vcs.Vendors.Github),
    JIRA("Jira (coming soon)", AllIcons.Nodes.Plugin),
    GITLAB("Gitlab (coming soon)", AllIcons.Nodes.Plugin),
    BITBUCKET("Bitbucket (coming soon)", AllIcons.Nodes.Plugin),
}

class AccountTypeListCellRenderer : ListCellRenderer<IssueTrackerType> {
    override fun getListCellRendererComponent(
        list: JList<out IssueTrackerType>,
        value: IssueTrackerType,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val label = JLabel()
        label.text = value.displayName
        label.icon = value.icon
        return label
    }
}

class IssueTrackerAccount(
) {
    var uniqueId: String = ""
    var type: IssueTrackerType = IssueTrackerType.SPACE
    var url: String = ""
    var username: String? = null
    var password: String? = null

    constructor(settings: String) : this() {
        val account = settings.split("|")
        this.type = IssueTrackerType.values()[account[0].toInt()]
        this.uniqueId = account[1]
        this.url = account[2]
        this.username = null
        this.password = null
    }

    constructor(uniqueId: String, type: IssueTrackerType, url: String, credentials: Credentials?) : this() {
        this.uniqueId = uniqueId
        this.type = type
        this.url = url
        if (credentials != null) {
            this.username = credentials.userName
            this.password = credentials.getPasswordAsString().orEmpty()
        }
    }

    override fun toString(): String {
        return "${type.ordinal}|$uniqueId|$url"
    }

    fun getCredentials(): Credentials {
        return Credentials(username, password)
    }
}

@Suppress("UnstableApiUsage")
class AdvancedIssueTrackerSettingsPanel : JPanel(), ActionListener {
    private val addAccountButton = JButton("Add Account")
    private val accountTypeComboBox = ComboBox<IssueTrackerType>()

    private val accountsList: ArrayList<IssueTrackerAccount> = ArrayList()

    private val accountsTable: JBTable

    init {
        this.loadAccounts()
        //this.createLayout()

        accountsTable = AccountsTable().apply {
            model = AccountsTableModel(accountsList.toTypedArray())

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            preferredScrollableViewportSize = Dimension(500, 150)
        }
        this.createNewLayout()

        // ComboBox should contain icons and text
        accountTypeComboBox.renderer = AccountTypeListCellRenderer()

        // Fill with data
        accountTypeComboBox.model = DefaultComboBoxModel(IssueTrackerType.values())

        // Check if we are in debug mode. If so we want to be able to select in-development features
        if (java.lang.management.ManagementFactory.getRuntimeMXBean().inputArguments.toString()
                .indexOf("-agentlib:jdwp") == 0
        ) {
            // Disable all but JetBrains Space for now
            accountTypeComboBox.isEnabled = false
            accountTypeComboBox.selectedIndex = 0
        }
    }

    private fun createNewLayout() {
        layout = GridLayoutManager(6, 1, JBUI.emptyInsets(), -1, -1)
        // Center this label
        this.CenteredLabel("Advanced Issue Tracker").apply {
            font = JBFont.h1().asBold()
        }
        this.Separator(maxSize = Dimension(300, 5))
        this.Label("Connections").apply {
            font = JBFont.h2().asBold()
        }

        val tablePanel = JPanel(BorderLayout())

        val scrollPane = JBScrollPane(
            accountsTable,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        )

        tablePanel.add(
            scrollPane
        )

        val toolbarDecorator: ToolbarDecorator = ToolbarDecorator.createDecorator(accountsTable)

        // The add account action is a popup showing the different account types
        val addAccountAction = object : DumbAwareAction("Add Account", "Add Account", AllIcons.General.Add) {
            override fun actionPerformed(e: AnActionEvent) {
                val popup = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Add Account",
                    DefaultActionGroup(
                        IssueTrackerType.values().map {
                            object : DumbAwareAction(it.displayName, "Only JetBrains Space is supported for now", it.icon) {
                                override fun actionPerformed(e: AnActionEvent) {
                                    val spaceAccountDialog = SpaceAccountDialog()
                                    val dlgResult = spaceAccountDialog.showAndGet()
                                    if (dlgResult) {
                                        val spaceAccountModel = spaceAccountDialog.getSpaceAccountModel()
                                        // Create a UUID derived from the URL
                                        val uniqueId = UUID.nameUUIDFromBytes(spaceAccountModel.url.toByteArray()).toString()
                                        val account = IssueTrackerAccount(
                                            uniqueId,
                                            IssueTrackerType.SPACE,
                                            spaceAccountModel.url,
                                            spaceAccountModel.getCredentials()
                                        )
                                        accountsList.add(account)
                                        accountsTable.model = AccountsTableModel(accountsList.toTypedArray())
                                    }
                                }
                            }.apply {
                                templatePresentation.isEnabled = it == IssueTrackerType.SPACE
                            }
                        }
                    ),
                    e.dataContext,
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    false
                )
                popup.showInBestPositionFor(e.dataContext)
            }
        }

        val editAccountAction = object : DumbAwareAction("Edit Account", "Edit Account", AllIcons.General.Inline_edit) {
            override fun actionPerformed(e: AnActionEvent) {
                val selectedRow = accountsTable.selectedRow
                if (selectedRow < 0) return
                val account = accountsList[selectedRow]
                /*val dialog = AccountDialog(account)
            dialog.show()
            if (dialog.isOK) {
                accountsList[selectedRow] = dialog.account
                accountsTable.model = AccountsTableModel(accountsList.toTypedArray())
            }*/
            }
        }

        val removeAccountAction = object : DumbAwareAction("Remove Account", "Remove Account", AllIcons.General.Remove) {
            override fun actionPerformed(e: AnActionEvent) {
                val selectedRow = accountsTable.selectedRow
                if (selectedRow < 0) return
                accountsList.removeAt(selectedRow)
                accountsTable.model = AccountsTableModel(accountsList.toTypedArray())
            }
        }

        val defaultActionGroup = DefaultActionGroup(addAccountAction, editAccountAction, removeAccountAction)
        toolbarDecorator.setActionGroup(defaultActionGroup)
        toolbarDecorator.setToolbarPosition(ActionToolbarPosition.BOTTOM)

        // Create table listener to enable/disable edit button
        accountsTable.selectionModel.addListSelectionListener {
            editAccountAction.templatePresentation.isEnabled = accountsTable.selectedRow >= 0
            removeAccountAction.templatePresentation.isEnabled = accountsTable.selectedRow >= 0
        }

        tablePanel.add(toolbarDecorator.createPanel())

        this.add(
            tablePanel,
            GridConstraints(
                3,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null,
                0,
                false
            )
        )

        this.Label("Visible Columns").apply {
            font = JBFont.h2().asBold()
        }

    }

    private fun loadAccounts() {
        PropertiesComponent.getInstance().getList("accounts")?.forEach {
            val issueTrackerAccount = IssueTrackerAccount(it)

            val credentialAttributes: CredentialAttributes =
                SettingsUtil.createCredentialAttributes(issueTrackerAccount.uniqueId)
            val credentials: Credentials = PasswordSafe.instance.get(credentialAttributes) ?: return@forEach

            issueTrackerAccount.username = credentials.userName
            issueTrackerAccount.password = credentials.getPasswordAsString().orEmpty()

            accountsList.add(issueTrackerAccount)
        }

        // If debug mode is enabled, add a dummy accounts
        if (ManagementFactory.getRuntimeMXBean().inputArguments.toString()
                .indexOf("-agentlib:jdwp") != 0
        ) {
            accountsList.add(
                IssueTrackerAccount(
                    "81c7a535-18dd-48f5-92ac-81593993e102",
                    IssueTrackerType.SPACE,
                    "https://space.com",
                    null
                )
            )
            accountsList.add(
                IssueTrackerAccount(
                    "1a17d62b-7bb9-4fae-a76b-e43d9e88f0a0",
                    IssueTrackerType.YOUTRACK,
                    "https://youtrack.com",
                    null
                )
            )
            accountsList.add(
                IssueTrackerAccount(
                    "1a17d62b-7bb9-4fae-a76b-e43d9e88f0a0",
                    IssueTrackerType.GITHUB,
                    "https://github.com",
                    null
                )
            )
            accountsList.add(
                IssueTrackerAccount(
                    "1a17d62b-7bb9-4fae-a76b-e43d9e88f0a0",
                    IssueTrackerType.JIRA,
                    "https://jira.com",
                    null
                )
            )
            accountsList.add(
                IssueTrackerAccount(
                    "1a17d62b-7bb9-4fae-a76b-e43d9e88f0a0",
                    IssueTrackerType.GITLAB,
                    "https://gitlab.com",
                    null
                )
            )
            accountsList.add(
                IssueTrackerAccount(
                    "1a17d62b-7bb9-4fae-a76b-e43d9e88f0a0",
                    IssueTrackerType.BITBUCKET,
                    "https://bitbucket.com",
                    null
                )
            )
        }
    }

    private fun saveAccounts() {
        val accountsSettings: ArrayList<String> =
            PropertiesComponent.getInstance().getList("accounts")?.toCollection(ArrayList()) ?: ArrayList()
        val accounts = accountsSettings.map(::IssueTrackerAccount)

        for (account in accountsList) {
            val credentialAttributes: CredentialAttributes = SettingsUtil.createCredentialAttributes(account.uniqueId)
            if (accounts.none { it.uniqueId == account.uniqueId }) {
                accountsSettings.add(account.toString())
            }

            // Check if the credentials have changed
            val credentials: Credentials? = PasswordSafe.instance.get(credentialAttributes)
            if (credentials != null) {
                if (credentials == account.getCredentials()) {
                    continue
                }
            }

            PasswordSafe.instance.set(credentialAttributes, account.getCredentials())
        }
    }

    override fun actionPerformed(e: ActionEvent?) {
        when (e?.source) {
            addAccountButton -> onAddAccount(e)
        }
    }

    private fun onAddAccount(e: ActionEvent?) {
        val accountType = accountTypeComboBox.selectedItem as IssueTrackerType
        println("Add account of type $accountType")

        // TODO: Show dialog popup
        when (accountType) {
            IssueTrackerType.SPACE -> SpaceAccountDialog().show()
            IssueTrackerType.GITHUB -> TODO()
            IssueTrackerType.YOUTRACK -> TODO() //return YouTrackAccountPanel()
            IssueTrackerType.JIRA -> TODO() //return JiraAccountPanel()
            IssueTrackerType.GITLAB -> TODO() //return GitLabAccountPanel()
            IssueTrackerType.BITBUCKET -> TODO() //return BitbucketAccountPanel()
        }
    }

    private fun createLayout() {
        this.layout = GridLayoutManager(9, 1, JBUI.insets(10), -1, -1)

        val settingsTitleLabel = JLabel()
        settingsTitleLabel.text = "Advanced Issue Tracker Settings"
        settingsTitleLabel.horizontalAlignment = SwingConstants.CENTER
        this.add(
            settingsTitleLabel, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null
            )
        )

        createAddAccountPanel()
        createAccountListPanel()
        createIssueColumnsList()

        val separator1 = JSeparator()
        this.add(
            separator1, GridConstraints(
                4,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
    }

    private fun createAddAccountPanel() {
        val accountTypePanel = JPanel()
        accountTypePanel.layout = GridLayoutManager(1, 2, JBUI.insets(0, 15), -1, -1, true, false)

        val accountTypeLabel = JLabel()
        accountTypeLabel.text = "Account Type:"
        accountTypePanel.add(
            accountTypeLabel, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null
            )
        )

        accountTypePanel.add(
            accountTypeComboBox, GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                Dimension(290, 30),
                null
            )
        )
        this.add(
            accountTypePanel, GridConstraints(
                1,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )

        addAccountButton.addActionListener(this)
        val addAccountPanel = JPanel()
        addAccountPanel.layout = GridLayoutManager(1, 2, JBUI.emptyInsets(), 0, 0)

        addAccountPanel.add(
            addAccountButton, GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_EAST,
                GridConstraints.FILL_VERTICAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        this.add(
            addAccountPanel, GridConstraints(
                2,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
    }

    private fun createAccountListPanel() {
        val accountsList = JBList<String>()
        val accountsScrollPane = JBScrollPane()
        accountsScrollPane.setViewportView(accountsList)
        this.add(
            accountsScrollPane, GridConstraints(
                3,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_WANT_GROW,
                null,
                Dimension(150, 50),
                null
            )
        )
    }

    private fun createIssueColumnsList() {

        val issueListColumnsLabel = JLabel()
        issueListColumnsLabel.text = "Issue List Columns"
        issueListColumnsLabel.horizontalAlignment = SwingConstants.CENTER

        this.add(
            issueListColumnsLabel, GridConstraints(
                5,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        val issueListLabelPanel = JPanel()
        issueListLabelPanel.layout = GridLayoutManager(1, 2, JBUI.emptyInsets(), 0, 0)

        val availableColumnsLabel = JLabel()
        availableColumnsLabel.text = "Available Columns"
        issueListLabelPanel.add(
            availableColumnsLabel, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )

        val activeColumnsLabel = JLabel()
        activeColumnsLabel.text = "Active Columns"
        issueListLabelPanel.add(
            activeColumnsLabel, GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        this.add(
            issueListLabelPanel, GridConstraints(
                6,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        val availableColumnsList = JBList<Any?>()

        val issueColumnsPanel = JPanel()
        issueColumnsPanel.layout = GridLayoutManager(1, 3, JBUI.emptyInsets(), 0, 0)

        val availableColumnsScrollPane = JBScrollPane()
        availableColumnsScrollPane.setViewportView(availableColumnsList)
        issueColumnsPanel.add(
            availableColumnsScrollPane, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )


        val issueColumnsActionButtonsPanel = JPanel()
        issueColumnsActionButtonsPanel.layout = GridLayoutManager(2, 1, JBUI.emptyInsets(), 0, 0)

        val issueButtonLeft = JButton()
        issueButtonLeft.text = "<<"
        issueColumnsActionButtonsPanel.add(
            issueButtonLeft, GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )

        val issueButtonRight = JButton()
        issueButtonRight.text = ">>"
        issueColumnsActionButtonsPanel.add(
            issueButtonRight, GridConstraints(
                1,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        issueColumnsPanel.add(
            issueColumnsActionButtonsPanel, GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )

        val activeColumnsScrollPane = JBScrollPane()
        val activeColumnsList = JBList<Any?>()
        activeColumnsScrollPane.setViewportView(activeColumnsList)
        issueColumnsPanel.add(
            activeColumnsScrollPane, GridConstraints(
                0,
                2,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
        this.add(
            issueColumnsPanel, GridConstraints(
                7,
                0,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                null,
                null,
                null
            )
        )
    }
}