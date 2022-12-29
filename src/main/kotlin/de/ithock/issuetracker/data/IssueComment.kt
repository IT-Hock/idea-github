package de.ithock.issuetracker.data

import org.kohsuke.github.GHIssueComment
import java.util.*

class IssueComment(
    val identifier: String,
    val body: String,
    val author: String,
    val createdAt: Date,
    val url: String
) {

    constructor(comment: GHIssueComment) : this(
        comment.id.toString(),
        comment.body,
        comment.user.login,
        comment.createdAt,
        comment.htmlUrl.toString()
    )
}