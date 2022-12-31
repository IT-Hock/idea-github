package de.ithock.advancedissuetracker.implementations.github

import de.ithock.advancedissuetracker.implementations.Issue
import de.ithock.advancedissuetracker.implementations.IssueTracker

class IssueTrackerGitHub(
    override val connection: GitHubConnection
) : IssueTracker(connection) {
    override fun createIssue(summary: String, description: String): Issue {
        TODO("Not yet implemented")
    }

    override fun getIssue(id: String): Issue? {
        TODO("Not yet implemented")
    }

    override fun getIssues() : List<Issue> {
        TODO("Not yet implemented")
    }

    override fun updateIssue(issue: Issue): Issue {
        TODO("Not yet implemented")
    }

    override fun deleteIssue(id: String): Boolean {
        TODO("Not yet implemented")
    }
}