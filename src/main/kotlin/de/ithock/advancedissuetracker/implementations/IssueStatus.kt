package de.ithock.advancedissuetracker.implementations

import com.intellij.ui.JBColor
import com.intellij.util.xmlb.annotations.OptionTag
import de.ithock.advancedissuetracker.util.ColorConverter
import java.awt.Color

open class IssueStatus() {
    var identifier: String = ""
    var name: String = ""
    var resolved: Boolean = false
    @OptionTag(converter = ColorConverter::class)
    var color: Color = JBColor.BLACK

    constructor(
        identifier: String,
        name: String,
        resolved: Boolean,
        color: Color
    ) : this() {
        this.identifier = identifier
        this.name = name
        this.resolved = resolved
        this.color = color
    }
}