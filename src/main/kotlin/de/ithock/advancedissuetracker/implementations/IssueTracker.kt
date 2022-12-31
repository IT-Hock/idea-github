package de.ithock.advancedissuetracker.implementations

abstract class IssueTracker(open val connection: IssueTrackerConnection) {
    abstract fun createIssue(summary: String, description: String): Issue
    abstract fun getIssue(id: String): Issue?

    abstract fun getIssues(): List<Issue>
    abstract fun updateIssue(issue: Issue): Issue
    abstract fun deleteIssue(id: String): Boolean
}