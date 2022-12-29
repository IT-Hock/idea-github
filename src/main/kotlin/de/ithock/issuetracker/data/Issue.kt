package de.ithock.issuetracker.data

import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import java.util.*

/**
 * A Class that represents an Issue
 * @param identifier The identifier of the issue
 * @param title The title of the issue
 * @param body The body of the issue
 * @param state The state of the issue
 * @param author The author of the issue
 * @param assignee The assignee of the issue
 * @param createdAt The creation date of the issue
 * @param closedAt The date when the issue was closed
 * @param labels The labels of the issue
 * @param milestone The milestone of the issue
 */
class Issue(
    identifier: String,
    title: String,
    body: String,
    state: IssueState,
    author: String,
    assignee: String?,
    createdAt: Date,
    closedAt: Date?,
    labels: List<IssueLabel>,
    milestone: String?,
    comments: List<IssueComment>,
    url: String
) {
    private val identifier: String
    private val title: String
    private val body: String
    private val state: IssueState

    private val milestone: String?
    private val author: String

    private val assignee: String?
    private val createdAt: Date

    private val closedAt: Date?
    /**
     * - Labels in GitHub
     * - Tags in JetBrains Space
     */
    private val labels: List<IssueLabel>

    private val comments: List<IssueComment>

    private val url: String

    init {
        this.identifier = identifier

        this.title = title
        this.body = body

        this.state = state
        this.milestone = milestone

        this.author = author
        this.assignee = assignee
        this.createdAt = createdAt
        this.closedAt = closedAt
        this.labels = labels
        this.comments = comments
        this.url = url
    }

    constructor(githubIssue: GHIssue) : this(
        githubIssue.number.toString(),
        githubIssue.title,
        githubIssue.body,
        if (githubIssue.state == GHIssueState.CLOSED) IssueState.CLOSED else IssueState.OPEN,
        githubIssue.user.login,
        githubIssue.assignee?.login,
        githubIssue.createdAt,
        githubIssue.closedAt,
        githubIssue.labels.map { IssueLabel(it) },
        githubIssue.milestone?.title,
        githubIssue.comments.map { IssueComment(it) },
        githubIssue.htmlUrl.toString()
    )

    fun getIdentifier(): String {
        return identifier
    }

    fun getTitle(): String {
        return title
    }

    fun getBody(): String {
        return body
    }

    fun getState(): IssueState {
        return state
    }

    fun getMilestone(): String? {
        return milestone
    }

    fun getAuthor(): String {
        return author
    }

    fun getAssignee(): String? {
        return assignee
    }

    fun getCreatedAt(): Date {
        return createdAt
    }

    fun getClosedAt(): Date? {
        return closedAt
    }

    fun getLabels(): List<IssueLabel> {
        return labels
    }

    fun getUrl(): String {
        return url
    }
}
