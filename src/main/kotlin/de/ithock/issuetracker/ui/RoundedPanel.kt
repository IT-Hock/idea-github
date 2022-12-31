package de.ithock.issuetracker.ui

import java.awt.*
import javax.swing.JPanel

internal class RoundedPanel : JPanel {
    private var backgroundColor: Color? = null
    private var cornerRadius = 15

    constructor(layout: LayoutManager?, radius: Int) : super(layout) {
        cornerRadius = radius
    }

    constructor(layout: LayoutManager?, radius: Int, bgColor: Color?) : super(layout) {
        cornerRadius = radius
        backgroundColor = bgColor
    }

    constructor(radius: Int) : super() {
        cornerRadius = radius
    }

    constructor(radius: Int, bgColor: Color?) : super() {
        cornerRadius = radius
        backgroundColor = bgColor
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val arcs = Dimension(cornerRadius, cornerRadius)
        val width: Int = width
        val height: Int = height
        val graphics: Graphics2D = g as Graphics2D
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        //Draws the rounded panel with borders.
        if (backgroundColor != null) {
            graphics.color = backgroundColor
        } else {
            graphics.color = background
        }
        graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height) //paint background
        graphics.color = foreground
        graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height) //paint border
    }
}