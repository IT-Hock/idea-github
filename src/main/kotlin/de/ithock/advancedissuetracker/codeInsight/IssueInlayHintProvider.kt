@file:Suppress("UnstableApiUsage")

package de.ithock.advancedissuetracker.codeInsight

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.ide.BrowserUtil
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.TokenSet
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.UIUtil
import de.ithock.advancedissuetracker.codeInsight.ui.IssueHover
import de.ithock.advancedissuetracker.implementations.Issue
import de.ithock.advancedissuetracker.implementations.fake.FakeIssue
import de.ithock.advancedissuetracker.util.PresentationFactoryEx
import de.ithock.issuetracker.PluginBundle
import de.ithock.issuetracker.util.Helpers
import de.ithock.issuetracker.util.Icons
import java.awt.Cursor
import java.awt.event.ActionEvent
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.util.*
import javax.swing.*

class IssueInlayHintProvider : InlayHintsProvider<IssueInlayHintProvider.Settings> {
    override val key: SettingsKey<Settings>
        get() = SettingsKey("advancedissuetracker.IssueInlayHintProvider")
    override val name: String
        get() = PluginBundle.get("inlay_hints.title")
    override val description: String
        get() = PluginBundle.get("inlay_hints.description")
    override val group: InlayGroup
        get() = InlayGroup.CODE_VISION_GROUP_NEW

    override fun getProperty(key: String): String {
        return PluginBundle.get("inlay_hints.$key")
    }

    constructor(language: Language){
        Helpers.getLogger().info("IssueInlayHintProvider for language $language")
    }

    override val previewText: String
        get() = """
            class Foo {
                // Issue FB-100
                // Issue #1234
                // Issue *1234
                // #1234
                // FB-100
                fun bar(fooVal:Int) : Int {
                    fooVal *= 4
                    return fooVal
                }
            }
        """.trimIndent()

    override fun isLanguageSupported(language: Language): Boolean {
        // Check if language has comment tokens using its parser
        LanguageParserDefinitions.INSTANCE.forLanguage(language)?.let { parserDefinition ->
            val commentTokens = parserDefinition.commentTokens
            return commentTokens != TokenSet.EMPTY
        }
        return false
    }

    override fun getCollectorFor(
        file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink
    ): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            val project = file.project
            val document = PsiDocumentManager.getInstance(project).getDocument(file)
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                // Check if the element is a comment with an issue reference
                val issueNumberRegex = Regex(settings.issueNumberRegex)
                // Check if the element is a comment
                if (element.node !is com.intellij.psi.PsiComment) {
                    return true
                }

                if (element.text.contains(issueNumberRegex)) {
                    if (editor !is EditorImpl) return false
                    // Regex to extract the issue number
                    val issueNumber = issueNumberRegex.find(element.text)!!.groupValues[1]

                    // TODO: Get issue
                    val issue = FakeIssue()

                    // Somehow the offset doesn't work in the preview?
                    val offset = element.startOffset
                    val inset = if (document == null) 0 else {
                        val width = EditorUtil.getPlainSpaceWidth(editor)
                        val line = document.getLineNumber(offset)
                        val startOffset = document.getLineStartOffset(line)
                        width * (offset - startOffset)
                    }

                    var inlayHint = createInlayHint(editor, issue)
                    inlayHint = factory.withCursorOnHover(
                        inlayHint,
                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                    )
                    inlayHint = factory.onClick(inlayHint, MouseButton.Left) { _, _ ->
                        BrowserUtil.browse(issue.url)
                    }
                    val customFactory = PresentationFactoryEx(editor)
                    inlayHint = customFactory.withTooltip(
                        IssueHover(
                            issue,
                            getIssueHoverActions(issue)
                        ),
                        inlayHint
                    )

                    sink.addBlockElement(
                        offset,
                        relatesToPrecedingText = false,
                        showAbove = true,
                        priority = 1,
                        presentation = factory.inset(inlayHint, left = inset)
                    )
                    return false
                }
                return true
            }


            private fun createInlayHint(editor: EditorImpl, issue: Issue): InlayPresentation {
                val inlayPresentations: MutableList<InlayPresentation> = mutableListOf()
                val factory = PresentationFactoryEx(editor)

                if (settings.showStatusAsIcon) {
                    val iconPresentation = factory.smallScaledIcon(Icons.IssueOpen)
                    inlayPresentations.add(
                        factory.inset(iconPresentation, left = -2, right = 5)
                    )
                }

                val textInlayPresentation = createInlayHintText(editor, getDisplayText(issue))
                inlayPresentations.add(textInlayPresentation)
                val combinedPresentation = factory.seq(*inlayPresentations.toTypedArray())

                val container: InlayPresentation = when {
                    settings.showStatusAsBackground -> {
                        val color =
                            editor.colorsScheme.getAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES).backgroundColor
                        factory.roundWithBackground(
                            combinedPresentation,
                            // Get warning color from editor
                            color
                        )
                    }

                    else -> {
                        factory.roundWithBackground(combinedPresentation)
                    }
                }

                return container
            }

            fun createInlayHintText(editor: EditorImpl, text: String): InlayPresentation {
                val factory = PresentationFactoryEx(editor)
                val textInlayPresentation = factory.smallText(text)
                return factory.container(
                    textInlayPresentation
                )
            }

            fun getDisplayText(issue: Issue): String {
                return getDisplayText(issue, settings)
            }
        }
    }

    private fun getIssueHoverActions(issue: Issue): List<Action> {
        val actions: MutableList<Action> = mutableListOf()

        actions.add(object : AbstractAction(PluginBundle.get("inlay_hints.actions.open_in_browser")) {
            override fun actionPerformed(e: ActionEvent?) {
                BrowserUtil.browse(issue.url)
            }
        })

        actions.add(object : AbstractAction(PluginBundle.get("inlay_hints.actions.edit_issue")) {
            override fun actionPerformed(e: ActionEvent?) {
                TODO("Not yet implemented")
            }
        })

        if (!issue.state.resolved) {
            actions.add(object : AbstractAction(PluginBundle.get("inlay_hints.actions.close_issue")) {
                override fun actionPerformed(e: ActionEvent?) {
                    TODO("Not yet implemented")
                }
            })
        } else {
            actions.add(object : AbstractAction(PluginBundle.get("inlay_hints.actions.reopen_issue")) {
                override fun actionPerformed(e: ActionEvent?) {
                    TODO("Not yet implemented")
                }
            })
        }
        return actions
    }

    fun getDisplayText(issue: Issue, settings: Settings): String {
        val displayText = StringBuilder()
        if (settings.showAuthor) {
            displayText.append("${issue.author.getFullName()}: ")
        }
        if (settings.showSummary) {
            // if length > settings.maxSummaryLength characters, truncate then ..., else show full

            displayText.append(
                if (issue.summary.length > settings.maxSummaryLength) {
                    issue.summary.substring(0, settings.maxSummaryLength) + "..."
                } else {
                    issue.summary
                }
            )
        } else {
            displayText.append("Issue #${issue.identifier}")
        }
        return displayText.toString()
    }

    override fun createConfigurable(settings: Settings): ImmediateConfigurable = object : ImmediateConfigurable {


        override fun createComponent(listener: ChangeListener): JComponent {
            val panel = JPanel(VerticalFlowLayout())
            val enableInlayHintCheckBox = JCheckBox(PluginBundle.get("inlay_hints.settings.show"))
            enableInlayHintCheckBox.isSelected = settings.showHints
            enableInlayHintCheckBox.addActionListener {
                settings.showHints = enableInlayHintCheckBox.isSelected
                listener.settingsChanged()
            }
            panel.add(enableInlayHintCheckBox)

            val showAsIconCheckBox = JCheckBox(PluginBundle.get("inlay_hints.settings.show_status_icon"))
            showAsIconCheckBox.isSelected = settings.showStatusAsIcon
            showAsIconCheckBox.addActionListener {
                settings.showStatusAsIcon = showAsIconCheckBox.isSelected
                listener.settingsChanged()
            }
            panel.add(showAsIconCheckBox)

            val showAuthorCheckBox = JCheckBox(PluginBundle.get("inlay_hints.settings.show_author"))
            showAuthorCheckBox.isSelected = settings.showAuthor
            showAuthorCheckBox.addActionListener {
                settings.showAuthor = showAuthorCheckBox.isSelected
                listener.settingsChanged()
            }
            panel.add(showAuthorCheckBox)

            val showDetailsOnHoverCheckBox = JCheckBox(PluginBundle.get("inlay_hints.settings.show_details_hover"))
            showDetailsOnHoverCheckBox.isSelected = settings.showDetailsOnHover
            showDetailsOnHoverCheckBox.addActionListener {
                settings.showDetailsOnHover = showDetailsOnHoverCheckBox.isSelected
                listener.settingsChanged()
            }
            panel.add(showDetailsOnHoverCheckBox)

            val showStatusAsBackgroundCheckBox =
                JCheckBox(PluginBundle.get("inlay_hints.settings.show_status_background"))
            showStatusAsBackgroundCheckBox.isSelected = settings.showStatusAsBackground
            showStatusAsBackgroundCheckBox.addActionListener {
                settings.showStatusAsBackground = showStatusAsBackgroundCheckBox.isSelected
                listener.settingsChanged()
            }
            panel.add(showStatusAsBackgroundCheckBox)

            val maxSummaryLengthLabel = JLabel(PluginBundle.get("inlay_hints.settings.max_summary_length"))
            panel.add(maxSummaryLengthLabel)

            val maxSummaryLengthSpinner = JSpinner(SpinnerNumberModel(settings.maxSummaryLength, 0, 100, 1))
            maxSummaryLengthSpinner.addChangeListener {
                settings.maxSummaryLength = maxSummaryLengthSpinner.value as Int
                listener.settingsChanged()
            }
            panel.add(maxSummaryLengthSpinner)

            // Create label for the JFormatedTextField
            val label = JBLabel(PluginBundle.get("inlay_hints.settings.issue_number_regex"))
            label.foreground = UIUtil.getLabelDisabledForeground()
            panel.add(label)

            // RegEx Text editor for issue number
            val issueNumberRegex = JBTextField(settings.issueNumberRegex)
            issueNumberRegex.columns = 25
            issueNumberRegex.inputVerifier = object : InputVerifier() {
                override fun verify(input: JComponent): Boolean {
                    // Verify if its valid regex
                    val regex = (input as JTextField).text
                    try {
                        Regex(regex)
                    } catch (e: Exception) {
                        return false
                    }
                    return true
                }
            }
            issueNumberRegex.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent?) {
                    settings.issueNumberRegex = issueNumberRegex.text
                    listener.settingsChanged()
                }
            })
            panel.add(issueNumberRegex)
            return panel
        }
    }

    override fun createSettings(): Settings {
        return Settings()
    }

    class Settings {
        var maxSummaryLength: Int = 50
        var showHints: Boolean = true
        var issueNumberRegex: String = "Issue #([0-9]+)"
        var showDetailsOnHover: Boolean = true
        var showStatusAsIcon: Boolean = true
        var showStatusAsBackground: Boolean = true

        var showAuthor: Boolean = true
        var showSummary: Boolean = true
        // TODO: Able to reorder and add the display
    }
}