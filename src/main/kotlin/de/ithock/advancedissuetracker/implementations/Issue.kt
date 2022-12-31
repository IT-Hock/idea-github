package de.ithock.advancedissuetracker.implementations

import de.ithock.issuetracker.data.IssueComment
import de.ithock.issuetracker.data.IssueLabel
import java.util.*

abstract class Issue() {
    var identifier: String = ""
    var summary: String = ""
    var body: String = ""
    var state: IssueStatus = IssueStatus()
    var milestone: String? = null
    lateinit var author: IssueUser
    var assignee: IssueUser? = null
    var createdAt: Date = Date()
    var closedAt: Date? = null
    var labels: List<IssueLabel> = listOf()
    var comments: List<IssueComment> = listOf()
    var url: String = ""
}