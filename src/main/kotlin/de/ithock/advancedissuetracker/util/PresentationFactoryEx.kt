@file:Suppress("UnstableApiUsage")

package de.ithock.advancedissuetracker.util

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.codeInsight.hints.InlayPresentationFactory
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.LightweightHint
import com.intellij.util.ui.JBUI
import de.ithock.advancedissuetracker.codeInsight.ui.IssueHover
import kotlinx.coroutines.*
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Contract
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import java.lang.Runnable
import java.util.*
import javax.swing.Icon
import javax.swing.JPanel
import kotlin.math.max

class PresentationFactoryEx(private val editor: EditorImpl) : InlayPresentationFactory {
    companion object {
        private val TEXT_METRICS_STORAGE = Key.create<InlayTextMetricsStorage>("InlayTextMetricsStorage")

        fun getTextMetricStorage(editor: EditorImpl): InlayTextMetricsStorage {
            val storage = editor.getUserData(TEXT_METRICS_STORAGE)
            if (storage == null) {
                val newStorage = InlayTextMetricsStorage(editor)
                editor.putUserData(TEXT_METRICS_STORAGE, newStorage)
                return newStorage
            }
            return storage
        }
    }

    private val textMetricsStorage = getTextMetricStorage(editor)
    private val offsetFromTopProvider = object : InsetValueProvider {
        override val top: Int
            get() = textMetricsStorage.getFontMetrics(true).offsetFromTop()
    }

    @Contract(pure = true)
    override fun smallText(text: String): InlayPresentation {
        val textWithoutBox = InsetPresentation(TextInlayPresentation(textMetricsStorage, true, text), top = 1, down = 1)
        return withInlayAttributes(textWithoutBox)
    }

    fun smallTextWithoutBackground(text: String): InlayPresentation {
        val textWithoutBox = InsetPresentation(TextInlayPresentation(textMetricsStorage, true, text), top = 1, down = 1)
        return WithAttributesPresentation(
            textWithoutBox, DefaultLanguageHighlighterColors.INLAY_TEXT_WITHOUT_BACKGROUND, editor,
            WithAttributesPresentation.AttributesFlags().withIsDefault(true)
        )
    }

    override fun container(
        presentation: InlayPresentation,
        padding: InlayPresentationFactory.Padding?,
        roundedCorners: InlayPresentationFactory.RoundedCorners?,
        background: Color?,
        backgroundAlpha: Float
    ): InlayPresentation {
        return ContainerInlayPresentation(presentation, padding, roundedCorners, background, backgroundAlpha)
    }

    override fun mouseHandling(
        base: InlayPresentation,
        clickListener: InlayPresentationFactory.ClickListener?,
        hoverListener: InlayPresentationFactory.HoverListener?
    ): InlayPresentation {
        return MouseHandlingPresentation(base, clickListener, hoverListener)
    }

    @ApiStatus.Experimental
    @Contract(pure = true)
    fun textSpacePlaceholder(length: Int, isSmall: Boolean): InlayPresentation {
        return TextPlaceholderPresentation(length, textMetricsStorage, isSmall)
    }

    @Contract(pure = true)
    override fun text(text: String): InlayPresentation {
        return withInlayAttributes(TextInlayPresentation(textMetricsStorage, false, text))
    }

    /**
     * Adds inlay background and rounding with insets.
     * Intended to be used with [smallText]
     */
    @Contract(pure = true)
    fun roundWithBackground(base: InlayPresentation, color: Color? = null, backgroundAlpha: Float = 0.55f): InlayPresentation {
        val rounding = withInlayAttributes(
            RoundWithBackgroundPresentation(
                InsetPresentation(
                    base,
                    left = 7,
                    right = 7,
                    top = 0,
                    down = 0
                ),
                8,
                8,
                color,
                backgroundAlpha
            )
        )
        return DynamicInsetPresentation(rounding, offsetFromTopProvider)
    }

    @Contract(pure = true)
    fun roundWithBackgroundAndSmallInset(base: InlayPresentation): InlayPresentation {
        val rounding = withInlayAttributes(
            RoundWithBackgroundPresentation(
                InsetPresentation(
                    base,
                    left = 3,
                    right = 3,
                    top = 0,
                    down = 0
                ),
                8,
                8
            )
        )
        return DynamicInsetPresentation(rounding, offsetFromTopProvider)
    }

    @Contract(pure = true)
    override fun icon(icon: Icon): IconPresentation = IconPresentation(icon, editor.component)

    @Contract(pure = true)
    override fun smallScaledIcon(icon: Icon): InlayPresentation {
        val iconWithoutBox = InsetPresentation(
            ScaledIconPresentation(textMetricsStorage, true, icon, editor.component),
            top = 1,
            down = 1
        )
        return withInlayAttributes(iconWithoutBox)
    }

    @Contract(pure = true)
    fun folding(placeholder: InlayPresentation, unwrapAction: () -> InlayPresentation): InlayPresentation {
        return ChangeOnClickPresentation(changeOnHover(placeholder, onHover = {
            attributes(placeholder, EditorColors.FOLDED_TEXT_ATTRIBUTES)
        }), onClick = unwrapAction)
    }

    @Contract(pure = true)
    fun inset(base: InlayPresentation, left: Int = 0, right: Int = 0, top: Int = 0, down: Int = 0): InsetPresentation {
        return InsetPresentation(base, left, right, top, down)
    }

    /**
     * Creates node, that can be collapsed/expanded by clicking on prefix/suffix.
     * If presentation is collapsed, clicking to content will expand it.
     */
    @Contract(pure = true)
    fun collapsible(
        prefix: InlayPresentation,
        collapsed: InlayPresentation,
        expanded: () -> InlayPresentation,
        suffix: InlayPresentation,
        startWithPlaceholder: Boolean = true
    ): InlayPresentation {
        val (matchingPrefix, matchingSuffix) = matchingBraces(prefix, suffix)
        var presentationToChange: BiStatePresentation? = null

        val content = BiStatePresentation(first = {
            onClick(collapsed, MouseButton.Left,
                onClick = { _, _ ->
                    presentationToChange?.flipState()
                })
        }, second = { expanded() }, initiallyFirstEnabled = startWithPlaceholder)
        presentationToChange = content

        val prefixExposed = OnClickPresentation(matchingPrefix) { _: MouseEvent, _: Point ->
            content.flipState()
        }
        val suffixExposed = OnClickPresentation(matchingSuffix) { _: MouseEvent, _: Point ->
            content.flipState()
        }
        return seq(prefixExposed, content, suffixExposed)
    }

    @Contract(pure = true)
    fun matchingBraces(left: InlayPresentation, right: InlayPresentation): Pair<InlayPresentation, InlayPresentation> {
        val (leftMatching, rightMatching) = matching(listOf(left, right))
        return leftMatching to rightMatching
    }

    @Contract(pure = true)
    fun matching(presentations: List<InlayPresentation>): List<InlayPresentation> =
        synchronousOnHover(presentations) { presentation ->
            attributes(presentation, CodeInsightColors.MATCHED_BRACE_ATTRIBUTES)
        }

    /**
     * On hover of any of [presentations] changes all the presentations with a given decorator.
     * This presentation is stateless.
     */
    @Contract(pure = true)
    fun synchronousOnHover(
        presentations: List<InlayPresentation>,
        decorator: (InlayPresentation) -> InlayPresentation
    ): List<InlayPresentation> {
        val forwardings = presentations.map { DynamicDelegatePresentation(it) }
        return forwardings.map {
            onHover(it, object : InlayPresentationFactory.HoverListener {
                override fun onHover(event: MouseEvent, translated: Point) {
                    for ((index, forwarding) in forwardings.withIndex()) {
                        forwarding.delegate = decorator(presentations[index])
                    }
                }

                override fun onHoverFinished() {
                    for ((index, forwarding) in forwardings.withIndex()) {
                        forwarding.delegate = presentations[index]
                    }
                }
            })
        }
    }

    /**
     * @see OnHoverPresentation
     */
    @Contract(pure = true)
    fun onHover(base: InlayPresentation, onHoverListener: InlayPresentationFactory.HoverListener): InlayPresentation =
        OnHoverPresentation(base, onHoverListener)

    /**
     * @see OnClickPresentation
     */
    @Contract(pure = true)
    fun onClick(base: InlayPresentation, button: MouseButton, onClick: (MouseEvent, Point) -> Unit): InlayPresentation {
        return OnClickPresentation(base) { e, p ->
            if (button == e.mouseButton) {
                onClick(e, p)
            }
        }
    }

    /**
     * @see OnClickPresentation
     */
    @Contract(pure = true)
    fun onClick(
        base: InlayPresentation,
        buttons: EnumSet<MouseButton>,
        onClick: (MouseEvent, Point) -> Unit
    ): InlayPresentation {
        return OnClickPresentation(base) { e, p ->
            if (e.mouseButton in buttons) {
                onClick(e, p)
            }
        }
    }

    /**
     * @see ChangeOnHoverPresentation
     */
    @Contract(pure = true)
    fun changeOnHover(
        base: InlayPresentation,
        onHover: () -> InlayPresentation,
        onHoverPredicate: (MouseEvent) -> Boolean = { true }
    ): InlayPresentation = ChangeOnHoverPresentation(base, onHover, onHoverPredicate)

    @Contract(pure = true)
    fun reference(base: InlayPresentation, onClickAction: () -> Unit): InlayPresentation {
        return referenceInternal(base, onClickAction)
    }

    @Contract(pure = true)
    fun referenceOnHover(
        base: InlayPresentation,
        clickListener: InlayPresentationFactory.ClickListener
    ): InlayPresentation {
        val hovered = onClick(
            base = withReferenceAttributes(base),
            buttons = EnumSet.of(MouseButton.Left, MouseButton.Middle),
            onClick = { e, p ->
                clickListener.onClick(e, p)
            }
        )
        val delegate = ChangeOnHoverPresentation(base, { hovered })
        return WithCursorOnHoverPresentation(delegate, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), editor)
    }

    private fun referenceInternal(
        base: InlayPresentation,
        onClickAction: () -> Unit,
        toStringProvider: (() -> String)? = null
    ) = reference(
        base = base,
        onClickAction = Runnable { onClickAction() },
        clickButtonsWithoutHover = EnumSet.of(MouseButton.Middle),
        clickButtonsWithHover = EnumSet.of(MouseButton.Left, MouseButton.Middle),
        hoverPredicate = { isControlDown(it) },
        toStringProvider = toStringProvider
    )

    @Contract(pure = true)
    private fun reference(
        base: InlayPresentation,
        onClickAction: Runnable,
        clickButtonsWithoutHover: EnumSet<MouseButton>,
        clickButtonsWithHover: EnumSet<MouseButton>,
        hoverPredicate: (MouseEvent) -> Boolean,
        toStringProvider: (() -> String)? = null
    ): InlayPresentation {
        val noHighlightReference = onClick(base, clickButtonsWithoutHover) { _, _ ->
            onClickAction.run()
        }
        return object : ChangeOnHoverPresentation(noHighlightReference, hover@{
            return@hover onClick(withReferenceAttributes(noHighlightReference), clickButtonsWithHover) { _, _ ->
                onClickAction.run()
            }
        }, hoverPredicate) {
            override fun toString(): String {
                if (toStringProvider != null) {
                    return "[${toStringProvider()}]${super.toString()}"
                }
                return super.toString()
            }
        }
    }

    @Contract(pure = true)
    fun withCursorOnHover(base: InlayPresentation, cursor: Cursor): InlayPresentation {
        return WithCursorOnHoverPresentation(base, cursor, editor)
    }

    @Contract(pure = true)
    fun withReferenceAttributes(noHighlightReference: InlayPresentation): WithAttributesPresentation {
        return attributes(
            noHighlightReference, EditorColors.REFERENCE_HYPERLINK_COLOR,
            WithAttributesPresentation.AttributesFlags().withSkipEffects(true)
        )
    }

    @Contract(pure = true)
    fun psiSingleReference(
        base: InlayPresentation,
        withDebugToString: Boolean = true,
        resolve: () -> PsiElement?
    ): InlayPresentation {
        return if (withDebugToString) {
            referenceInternal(
                base,
                onClickAction = { navigateInternal(resolve) },
                toStringProvider = {
                    val element = resolve() ?: return@referenceInternal ""
                    val virtualFile = element.containingFile.virtualFile
                    val path = (virtualFile.fileSystem as? JarFileSystem)?.let {
                        val root = VfsUtilCore.getRootFile(virtualFile)
                        "${it.protocol}://${root.name}${JarFileSystem.JAR_SEPARATOR}${
                            VfsUtilCore.getRelativeLocation(
                                virtualFile,
                                root
                            )
                        }"
                    } ?: virtualFile.toString()
                    return@referenceInternal "$path:${element.startOffset}"
                })
        } else {
            reference(base) { navigateInternal(resolve) }
        }
    }

    @Contract(pure = true)
    fun psiSingleReference(base: InlayPresentation, resolve: () -> PsiElement?): InlayPresentation {
        return reference(base) { navigateInternal(resolve) }
    }

    @Contract(pure = true)
    fun seq(vararg presentations: InlayPresentation): InlayPresentation {
        return when (presentations.size) {
            0 -> SpacePresentation(0, 0)
            1 -> presentations.first()
            else -> SequencePresentation(presentations.toList())
        }
    }

    fun join(presentations: List<InlayPresentation>, separator: () -> InlayPresentation): InlayPresentation {
        val seq = mutableListOf<InlayPresentation>()
        var first = true
        for (presentation in presentations) {
            if (!first) {
                seq.add(separator())
            }
            seq.add(presentation)
            first = false
        }
        return SequencePresentation(seq)
    }

    fun button(
        default: InlayPresentation,
        clicked: InlayPresentation,
        clickListener: InlayPresentationFactory.ClickListener?,
        hoverListener: InlayPresentationFactory.HoverListener?,
        initialState: Boolean = false
    ): Pair<InlayPresentation, BiStatePresentation> {
        val defaultOrClicked: BiStatePresentation =
            object : BiStatePresentation({ default }, { clicked }, initialState) {
                override val width: Int
                    get() = max(default.width, clicked.width)

                override val height: Int
                    get() = max(default.height, clicked.height)
            }
        return object : StaticDelegatePresentation(defaultOrClicked) {
            override fun mouseClicked(event: MouseEvent, translated: Point) {
                clickListener?.onClick(event, translated)
                defaultOrClicked.flipState()
            }

            override fun mouseMoved(event: MouseEvent, translated: Point) {
                hoverListener?.onHover(event, translated)
            }

            override fun mouseExited() {
                hoverListener?.onHoverFinished()
            }
        } to defaultOrClicked
    }

    private fun attributes(
        base: InlayPresentation,
        textAttributesKey: TextAttributesKey,
        flags: WithAttributesPresentation.AttributesFlags = WithAttributesPresentation.AttributesFlags()
    ): WithAttributesPresentation =
        WithAttributesPresentation(base, textAttributesKey, editor, flags)

    private fun withInlayAttributes(base: InlayPresentation): InlayPresentation {
        return WithAttributesPresentation(
            base, DefaultLanguageHighlighterColors.INLAY_DEFAULT, editor,
            WithAttributesPresentation.AttributesFlags().withIsDefault(true)
        )
    }

    private fun isControlDown(e: MouseEvent): Boolean = (SystemInfo.isMac && e.isMetaDown) || e.isControlDown

    @Contract(pure = true)
    fun withTooltip(@NlsContexts.HintText tooltip: String, base: InlayPresentation): InlayPresentation = when {
        tooltip.isEmpty() -> base
        else -> {
            var hint: LightweightHint? = null
            onHover(base, object : InlayPresentationFactory.HoverListener {
                override fun onHover(event: MouseEvent, translated: Point) {
                    if (hint?.isVisible != true && editor.contentComponent.isShowing) {
                        hint = showTooltip(editor, event, tooltip)
                    }
                }

                override fun onHoverFinished() {
                    hint?.hide()
                    hint = null
                }
            })
        }
    }

    private fun showTooltip(editor: Editor, e: MouseEvent, @NlsContexts.HintText text: String): LightweightHint {
        val hint = run {
            val label = HintUtil.createInformationLabel(text)
            label.border = JBUI.Borders.empty(6, 6, 5, 6)
            LightweightHint(label)
        }

        val constraint = HintManager.ABOVE

        val point = run {
            val pointOnEditor = locationAt(e, editor.contentComponent)
            val p = HintManagerImpl.getHintPosition(hint, editor, editor.xyToVisualPosition(pointOnEditor), constraint)
            p.x = e.xOnScreen - editor.contentComponent.topLevelAncestor.locationOnScreen.x
            p
        }

        HintManagerImpl.getInstanceImpl().showEditorHint(
            hint, editor, point,
            HintManager.HIDE_BY_ANY_KEY
                    or HintManager.HIDE_BY_TEXT_CHANGE
                    or HintManager.HIDE_BY_SCROLLING,
            0,
            false,
            HintManagerImpl.createHintHint(editor, point, hint, constraint).setContentActive(false)
        )

        return hint
    }

    private fun locationAt(e: MouseEvent, component: Component): Point {
        val pointOnScreen = component.locationOnScreen
        return Point(e.xOnScreen - pointOnScreen.x, e.yOnScreen - pointOnScreen.y)
    }

    private fun navigateInternal(resolve: () -> PsiElement?) {
        val target = resolve()
        if (target is Navigatable) {
            CommandProcessor.getInstance().executeCommand(target.project, { target.navigate(true) }, null, null)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Contract(pure = true)
    fun withTooltip(issueHover: IssueHover, base: InlayPresentation): InlayPresentation {
        var hint: LightweightHint? = null
        var hoverJob: Job? = null

        return onHover(base, object : InlayPresentationFactory.HoverListener {
            override fun onHover(event: MouseEvent, translated: Point) {
                if (hoverJob != null) return
                hoverJob = GlobalScope.launch {
                    delay(500L)
                    ApplicationManager.getApplication().invokeLater {
                        if (hint?.isVisible != true) {
                            issueHover.onActionInvoked(object : IssueHover.ActionInvokedListener {
                                override fun onActionInvoked() {
                                    hint?.hide()
                                    hint = null
                                }
                            })
                            hint = showTooltip(editor, event, issueHover)
                        }
                    }
                }
            }

            override fun onHoverFinished() {
                hoverJob?.cancel()
                hoverJob = null
                // hint?.hide()
                // hint = null
            }
        })
    }

    private fun showTooltip(editor: Editor, e: MouseEvent, tooltip: JPanel): LightweightHint? {
        val hint = run {
            tooltip.border = JBUI.Borders.empty(6, 6, 5, 6)
            LightweightHint(tooltip)
        }

        val constraint = HintManager.ABOVE

        val point = run {
            try {
                val pointOnEditor = locationAt(e, editor.contentComponent)
                val p =
                    HintManagerImpl.getHintPosition(hint, editor, editor.xyToVisualPosition(pointOnEditor), constraint)
                p.x = e.xOnScreen - editor.contentComponent.topLevelAncestor.locationOnScreen.x
                p
            } catch (e: Throwable) {
                null
            }
        } ?: return null

        HintManagerImpl.getInstanceImpl().showEditorHint(hint, editor, point,
            HintManager.HIDE_BY_ANY_KEY
                    or HintManager.HIDE_BY_TEXT_CHANGE
                    or HintManager.HIDE_BY_SCROLLING,
            0,
            false,
            HintManagerImpl.createHintHint(editor, point, hint, constraint).setContentActive(false)
        )

        return hint
    }
}