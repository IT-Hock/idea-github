package de.ithock.issuetracker.data

import org.kohsuke.github.GHLabel

class IssueLabel(
    identifier: String,
    name: String,
    color: String
) {

    private val identifier: String
    private val name: String
    private val color: String

    init {
        this.identifier = identifier
        this.name = name
        this.color = color
    }

    constructor(label: GHLabel) : this(label.id.toString(), label.name, label.color)

    fun getName(): String {
        return name
    }

    fun getColor(): String {
        return color
    }

    fun getIdentifier(): String {
        return identifier
    }
}