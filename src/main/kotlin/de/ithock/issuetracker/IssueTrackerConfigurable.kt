package de.ithock.issuetracker

import com.intellij.ide.ui.UINumericRange
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.kohsuke.github.GitHub
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*

/**
 * Issue tracker configurable
 *
 * It has the following settings:
 * - GitHub OAuth token
 * - Date format
 * - Columns to display
 *
 * @constructor Create empty Issue tracker configurable
 */
class IssueTrackerConfigurable : Configurable {
    private lateinit var panel: JPanel
    private lateinit var tokenField: JPasswordField
    private lateinit var generateTokenButton: JButton
    private lateinit var dateFormatComboBox: JComboBox<String>
    private lateinit var availableColumnsList: JList<String>
    private lateinit var displayedColumnsList: JList<String>
    private lateinit var addButton: JButton
    private lateinit var removeButton: JButton
    private lateinit var ascendingSortRadioButton: JRadioButton
    private lateinit var descendingSortRadioButton: JRadioButton
    private lateinit var openColorButton: JButton
    private lateinit var closedColorButton: JButton

    private var openColor: Color = JBColor(0x00FF00, 0x00FF00)
    private var closedColor: Color = JBColor(0xFF0000, 0xFF0000)
    override fun createComponent(): @Nls(capitalization = Nls.Capitalization.Title) JComponent {
        panel = JPanel(GridBagLayout())
        createTokenField()
        createDateFormatComboBox()
        createColumnLists()
        createSortRadioButtons()
        createColorButtons()
        return panel
    }

    private fun createTokenField() {
        tokenField = JPasswordField()

        panel.add(
            JLabel("GitHub OAuth Token:"),
            GridBagConstraints(
                0,
                0,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.emptyInsets(),
                0,
                0
            )
        )

        panel.add(
            tokenField,
            GridBagConstraints(
                1,
                0,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                JBUI.insetsLeft(10),
                0,
                0
            )
        )

        generateTokenButton = JButton("Generate Token")
        generateTokenButton.addActionListener { e: ActionEvent? -> generateToken() }
        panel.add(
            generateTokenButton,
            GridBagConstraints(
                2,
                0,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.insetsLeft(10),
                0,
                0
            )
        )
    }

    private fun createDateFormatComboBox() {
        // Create two arrays, one for date formats the other for time formats.
        // DD.MM.YYYY, MM/DD/YYYY, YYYY-MM-DD, DD/MM/YYYY, YYYY/MM/DD
        val dateFormats = arrayOf(
            "dd.MM.yyyy",
            "MM/dd/yyyy",
            "yyyy-MM-dd",
            "dd/MM/yyyy",
            "yyyy/MM/dd"
        )

        // HH:mm:ss, HH:mm, HH:mm:ss a, HH:mm a, x time ago
        val timeFormats = arrayOf(
            "HH:mm:ss",
            "HH:mm",
            "HH:mm:ss a",
            "HH:mm a"
        )

        // Create a new array with the date formats and time formats combined.
        val dateFormatStrings = arrayOfNulls<String>(dateFormats.size * timeFormats.size + 1)
        dateFormatStrings[0] = "x time ago"
        var i = 1
        for (dateFormat in dateFormats) {
            for (timeFormat in timeFormats) {
                SimpleDateFormat("$dateFormat $timeFormat").let {
                    dateFormatStrings[i] = it.format(Date())
                }
                i++
            }
        }

        dateFormatComboBox = ComboBox(dateFormatStrings)
        panel.add(
            JLabel("Date Format:"),
            GridBagConstraints(
                0,
                1,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.insetsTop(10),
                0,
                0
            )
        )
        panel.add(
            dateFormatComboBox,
            GridBagConstraints(
                1,
                1,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                JBUI.insets(10, 10, 0, 0),
                0,
                0
            )
        )
    }

    private fun createColumnLists() {
        availableColumnsList = JBList()
        availableColumnsList.setListData(arrayOf("ID", "Title", "State", "Labels"))
        availableColumnsList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        displayedColumnsList = JBList()
        displayedColumnsList.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        // Add the add and remove buttons
        addButton = JButton("Add")
        addButton.addActionListener { e: ActionEvent? -> addSelectedColumns() }
        panel.add(
            addButton,
            GridBagConstraints(
                2,
                2,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.insets(10, 10, 0, 0),
                0,
                0
            )
        )
        removeButton = JButton("Remove")
        removeButton.addActionListener { e: ActionEvent? -> removeSelectedColumns() }
        panel.add(
            removeButton,
            GridBagConstraints(
                2,
                3,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.insets(10, 10, 0, 0),
                0,
                0
            )
        )

        // Add the ability to reorder the columns
        displayedColumnsList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(evt: MouseEvent) {
                val list = evt.source as JList<*>
                if (evt.clickCount == 2) {
                    // Double-click detected
                    val index = list.locationToIndex(evt.point)
                    val selectedValue = displayedColumnsList.selectedValue
                    //Extension.getInstance().removeDisplayedColumn(selectedValue);
                    //Extension.getInstance().addDisplayedColumn(index, selectedValue);
                    //displayedColumnsList.setListData(Extension.getInstance().getDisplayedColumns());
                }
            }
        })
        displayedColumnsList.componentPopupMenu = createPopupMenu()
    }

    private fun createPopupMenu(): JPopupMenu {
        val menu = JPopupMenu()
        val moveUpItem = JMenuItem("Move Up")
        moveUpItem.addActionListener { e: ActionEvent? -> moveSelectedColumnUp() }
        menu.add(moveUpItem)
        val moveDownItem = JMenuItem("Move Down")
        moveDownItem.addActionListener { e: ActionEvent? -> moveSelectedColumnDown() }
        menu.add(moveDownItem)
        return menu
    }

    private fun moveSelectedColumnUp() {
        val selectedIndices = displayedColumnsList.selectedIndices
        if (selectedIndices.isEmpty() || selectedIndices[0] == 0) {
            return
        }
        val displayedColumns = arrayOfNulls<String>(0) //Extension.getInstance().getDisplayedColumns();
        for (index in selectedIndices) {
            val temp = displayedColumns[index - 1]
            displayedColumns[index - 1] = displayedColumns[index]
            displayedColumns[index] = temp
        }
        //        Extension.getInstance().setDisplayedColumns(displayedColumns);
        displayedColumnsList.setListData(displayedColumns)
        displayedColumnsList.selectedIndices = Arrays.stream(selectedIndices).map { i: Int -> i - 1 }.toArray()
    }

    private fun moveSelectedColumnDown() {
        val selectedIndices = displayedColumnsList.selectedIndices
        if (selectedIndices.isEmpty() || selectedIndices[selectedIndices.size - 1] == displayedColumnsList.model.size - 1) {
            return
        }
        val displayedColumns = arrayOfNulls<String>(0) //Extension.getInstance().getDisplayedColumns();
        for (i in selectedIndices.indices.reversed()) {
            val index = selectedIndices[i]
            val temp = displayedColumns[index + 1]
            displayedColumns[index + 1] = displayedColumns[index]
            displayedColumns[index] = temp
        }
        //        Extension.getInstance().setDisplayedColumns(displayedColumns);
        displayedColumnsList.setListData(displayedColumns)
        displayedColumnsList.selectedIndices = Arrays.stream(selectedIndices).map { i: Int -> i + 1 }.toArray()
    }

    private fun addSelectedColumns() {
        val selectedValues = availableColumnsList.selectedValuesList.toTypedArray()
        //        Extension.getInstance().addDisplayedColumns(selectedValues);
//        displayedColumnsList.setListData(Extension.getInstance().getDisplayedColumns());
    }

    private fun removeSelectedColumns() {
        val selectedValues = displayedColumnsList.selectedValuesList.toTypedArray()
        //        Extension.getInstance().removeDisplayedColumns(selectedValues);
//        displayedColumnsList.setListData(Extension.getInstance().getDisplayedColumns());
    }

    private fun createSortRadioButtons() {
        ascendingSortRadioButton = JRadioButton("Ascending")
        descendingSortRadioButton = JRadioButton("Descending")
        val sortGroup = ButtonGroup()
        sortGroup.add(ascendingSortRadioButton)
        sortGroup.add(descendingSortRadioButton)
        /*if (Extension.getInstance().isAscendingSort()) {
            ascendingSortRadioButton.setSelected(true);
        } else {
            descendingSortRadioButton.setSelected(true);
        }*/panel.add(
            JLabel("Sort Order:"),
            GridBagConstraints(
                0,
                4,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.insetsTop(10),
                0,
                0
            )
        )
        panel.add(
            ascendingSortRadioButton,
            GridBagConstraints(
                1,
                4,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                JBUI.insets(10, 10, 0, 0),
                0,
                0
            )
        )
        panel.add(
            descendingSortRadioButton,
            GridBagConstraints(
                1,
                5,
                1,
                1,
                1.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                JBUI.insetsLeft(10),
                0,
                0
            )
        )
    }

    private fun createColorButtons() {
//        openColor = Extension.getInstance().getOpenColor();
//        closedColor = Extension.getInstance().getClosedColor();
        openColorButton = JButton("Open Color")
        openColorButton.background = openColor
        openColorButton.addActionListener { e: ActionEvent? -> openColor = chooseColor(openColorButton) }
        panel.add(
            openColorButton,
            GridBagConstraints(
                0,
                6,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.insetsTop(10),
                0,
                0
            )
        )
        closedColorButton = JButton("Closed Color")
        closedColorButton.background = closedColor
        closedColorButton.addActionListener { e: ActionEvent? ->
            closedColor = chooseColor(
                closedColorButton!!
            )
        }
        panel.add(
            closedColorButton,
            GridBagConstraints(
                0,
                7,
                1,
                1,
                0.0,
                0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                JBUI.insetsTop(10),
                0,
                0
            )
        )
    }

    private fun chooseColor(button: JButton): Color {
        val color = JColorChooser.showDialog(panel, "Choose Color", button.background)
        if (color != null) {
            button.background = color
        }
        return color
    }

    private fun generateToken() {
        val clientId = JOptionPane.showInputDialog(panel, "Enter your GitHub Client ID:")
        val clientSecret = JOptionPane.showInputDialog(panel, "Enter your GitHub Client Secret:")
        try {
            // Create collection with repository and organization scopes
            val scopes: MutableCollection<String> = ArrayList()
            scopes.add("repo")
            scopes.add("read:org") // This is required to access private repositories
            val github = GitHub.connectUsingOAuth(clientId, clientSecret)
            val repository = github.getUser("USERNAME").getRepository("REPO_NAME")
            val organization = github.getOrganization("ORGANIZATION_NAME")
            val authorization = github.createToken(scopes, "Advanced IssueTracker Extension", "https://it-hock.de/ait")
            tokenField.text = authorization.token
        } catch (e: IOException) {
            JOptionPane.showMessageDialog(
                panel,
                "Error generating token: " + e.message,
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    override fun isModified(): Boolean {
        /*return !tokenField.getText().equals(Extension.getInstance().getToken()) ||
                !dateFormatComboBox.getSelectedItem().equals(Extension.getInstance().getDateFormat()) ||
                !Arrays.equals(availableColumnsList.getSelectedValuesList().toArray(new String[0]), Extension.getInstance().getDisplayedColumns()) ||
                (ascendingSortRadioButton.isSelected() != Extension.getInstance().isAscendingSort()) ||
                !openColor.equals(Extension.getInstance().getOpenColor()) ||
                !closedColor.equals(Extension.getInstance().getClosedColor());*/
        return false
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
//        Extension.getInstance().setToken(tokenField.getText());
//        Extension.getInstance().setDateFormat((String) dateFormatComboBox.getSelectedItem());
//        Extension.getInstance().setDisplayedColumns(displayedColumnsList.getModel().getSize() == 0 ? new String[0] : displayedColumnsList.getSelectedValuesList().toArray(new String[0]));
//        Extension.getInstance().setAscendingSort(ascendingSortRadioButton.isSelected());
//        Extension.getInstance().setOpenColor(openColor);
//        Extension.getInstance().setClosedColor(closedColor);
    }

    override fun reset() {
//        tokenField.setText(Extension.getInstance().getToken());
//        dateFormatComboBox.setSelectedItem(Extension.getInstance().getDateFormat());
//        displayedColumnsList.setListData(Extension.getInstance().getDisplayedColumns());
//        if (Extension.getInstance().isAscendingSort()) {
//            ascendingSortRadioButton.setSelected(true);
//        } else {
//            descendingSortRadioButton.setSelected(true);
//        }
//        openColorButton.setBackground(Extension.getInstance().getOpenColor());
//        closedColorButton.setBackground(Extension.getInstance().getClosedColor());
    }

    override fun getDisplayName(): String {
        return "Advanced IssueTracker"
    }
}