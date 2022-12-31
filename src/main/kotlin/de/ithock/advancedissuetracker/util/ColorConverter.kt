package de.ithock.advancedissuetracker.util

import com.intellij.util.xmlb.Converter
import java.awt.Color

class ColorConverter : Converter<Color>() {
    override fun toString(value: Color): String {
        return "#" + Integer.toHexString(value.rgb)
    }

    override fun fromString(value: String): Color {
        return Color.decode(value)
    }
}